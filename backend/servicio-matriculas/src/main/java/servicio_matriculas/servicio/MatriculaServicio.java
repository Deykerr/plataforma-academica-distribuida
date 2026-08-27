package servicio_matriculas.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_matriculas.dominio.EstadoMatricula;
import servicio_matriculas.dominio.EstadoPeriodo;
import servicio_matriculas.dominio.EstadoSeccion;
import servicio_matriculas.dominio.HorarioSeccion;
import servicio_matriculas.dominio.Matricula;
import servicio_matriculas.dominio.Periodo;
import servicio_matriculas.dominio.Seccion;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.integracion.CursoValidacion;
import servicio_matriculas.dto.integracion.UsuarioValidacion;
import servicio_matriculas.dto.integracion.ValidacionPrerrequisitos;
import servicio_matriculas.dto.matricula.CrearMatriculaSolicitud;
import servicio_matriculas.dto.matricula.MatriculaRespuesta;
import servicio_matriculas.dto.matricula.ValidacionMatriculaRespuesta;
import servicio_matriculas.excepcion.ConflictoException;
import servicio_matriculas.excepcion.RecursoNoEncontradoException;
import servicio_matriculas.excepcion.ReglaNegocioException;
import servicio_matriculas.integracion.IntegracionAcademicaCliente;
import servicio_matriculas.repositorio.MatriculaRepositorio;
import servicio_matriculas.seguridad.ContextoUsuario;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaServicio {

    private final MatriculaRepositorio matriculaRepositorio;
    private final SeccionServicio seccionServicio;
    private final IntegracionAcademicaCliente integracion;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public MatriculaRespuesta crear(CrearMatriculaSolicitud solicitud) {
        contextoUsuario.exigirPropietarioOAdministrador(solicitud.estudianteId());

        UsuarioValidacion estudiante = integracion.validarUsuario(solicitud.estudianteId(), "ESTUDIANTE");
        if (!estudiante.existe() || !estudiante.activo()) {
            throw new ReglaNegocioException("El estudiante no existe, esta inactivo o no tiene ese rol");
        }

        Seccion seccion = seccionServicio.bloquear(solicitud.seccionId());
        validarApertura(seccion);
        CursoValidacion curso = integracion.validarCurso(seccion.getCursoId());
        if (!curso.existe() || !curso.activo()) {
            throw new ReglaNegocioException("El curso de la seccion ya no esta activo");
        }
        validarPrerrequisitos(solicitud.estudianteId(), curso);

        if (matriculaRepositorio.existsByEstudianteIdAndSeccionIdAndEstado(solicitud.estudianteId(),
                seccion.getId(), EstadoMatricula.ACTIVA)) {
            throw new ConflictoException("El estudiante ya esta matriculado en esta seccion");
        }
        if (matriculaRepositorio.existsByEstudianteIdAndPeriodoIdAndCursoIdAndEstado(
                solicitud.estudianteId(), seccion.getPeriodo().getId(), seccion.getCursoId(),
                EstadoMatricula.ACTIVA)) {
            throw new ConflictoException(
                    "El estudiante ya tiene una matricula activa para este curso en el periodo");
        }
        long ocupados = seccionServicio.ocupados(seccion.getId());
        if (ocupados >= seccion.getCapacidad()) {
            throw new ConflictoException("La seccion ya no tiene vacantes disponibles");
        }
        validarChoqueEstudiante(solicitud.estudianteId(), seccion);

        return respuesta(matriculaRepositorio.save(new Matricula(solicitud.estudianteId(), seccion)));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<MatriculaRespuesta> listar(Long estudianteId, Long periodoId,
                                                       Long seccionId, EstadoMatricula estado,
                                                       Pageable pageable) {
        Specification<Matricula> spec = (root, query, cb) -> cb.conjunction();
        if (estudianteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estudianteId"), estudianteId));
        }
        if (periodoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("periodo").get("id"), periodoId));
        }
        if (seccionId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("seccion").get("id"), seccionId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(matriculaRepositorio.findAll(spec, pageable).map(this::respuesta));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<MatriculaRespuesta> mias(Long periodoId, EstadoMatricula estado,
                                                     Pageable pageable) {
        return listar(contextoUsuario.usuarioId(), periodoId, null, estado, pageable);
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<MatriculaRespuesta> porSeccion(Long seccionId, Pageable pageable) {
        Seccion seccion = seccionServicio.buscarEntidad(seccionId);
        boolean administrador = contextoUsuario.tieneRol("ADMINISTRADOR");
        boolean docenteResponsable = contextoUsuario.tieneRol("DOCENTE")
                && contextoUsuario.usuarioId().equals(seccion.getDocenteId());
        if (!administrador && !docenteResponsable) {
            throw new AccessDeniedException(
                    "Solo el docente responsable o la administracion pueden consultar la seccion");
        }
        return listar(null, null, seccionId, EstadoMatricula.ACTIVA, pageable);
    }

    @Transactional(readOnly = true)
    public MatriculaRespuesta obtener(Long id) {
        Matricula matricula = buscar(id);
        exigirConsultaPermitida(matricula);
        return respuesta(matricula);
    }

    @Transactional
    public MatriculaRespuesta retirar(Long id, String motivo) {
        Matricula matricula = buscar(id);
        contextoUsuario.exigirPropietarioOAdministrador(matricula.getEstudianteId());
        validarActiva(matricula);
        if (matricula.getPeriodo().getEstado() == EstadoPeriodo.FINALIZADO
                || matricula.getPeriodo().getEstado() == EstadoPeriodo.CANCELADO) {
            throw new ReglaNegocioException("No se puede retirar una matricula de un periodo cerrado");
        }
        matricula.retirar(EstadoMatricula.RETIRADA, limpiar(motivo));
        return respuesta(matricula);
    }

    @Transactional
    public MatriculaRespuesta anular(Long id, String motivo) {
        Matricula matricula = buscar(id);
        validarActiva(matricula);
        matricula.retirar(EstadoMatricula.ANULADA, limpiar(motivo));
        return respuesta(matricula);
    }

    @Transactional
    public MatriculaRespuesta completar(Long id) {
        Matricula matricula = buscar(id);
        validarActiva(matricula);
        if (matricula.getSeccion().getEstado() != EstadoSeccion.FINALIZADA
                && matricula.getPeriodo().getEstado() != EstadoPeriodo.FINALIZADO) {
            throw new ReglaNegocioException(
                    "La matricula solo puede completarse cuando la seccion o el periodo finalice");
        }
        matricula.completar();
        return respuesta(matricula);
    }

    @Transactional(readOnly = true)
    public ValidacionMatriculaRespuesta validar(Long id) {
        return matriculaRepositorio.findById(id)
                .map(m -> new ValidacionMatriculaRespuesta(m.getId(), true,
                        m.getEstado() == EstadoMatricula.ACTIVA
                                && m.getPeriodo().getEstado() != EstadoPeriodo.CANCELADO,
                        m.getEstado(), m.getEstudianteId(), m.getSeccion().getId(),
                        m.getPeriodo().getId(), m.getCursoId()))
                .orElseGet(() -> new ValidacionMatriculaRespuesta(id, false, false,
                        null, null, null, null, null));
    }

    private void validarApertura(Seccion seccion) {
        Periodo periodo = seccion.getPeriodo();
        LocalDate hoy = LocalDate.now();
        if (periodo.getEstado() != EstadoPeriodo.MATRICULA_ABIERTA
                || hoy.isBefore(periodo.getFechaInicioMatricula())
                || hoy.isAfter(periodo.getFechaFinMatricula())) {
            throw new ReglaNegocioException("El periodo no tiene la matricula vigente");
        }
        if (seccion.getEstado() != EstadoSeccion.ABIERTA) {
            throw new ReglaNegocioException("La seccion no esta abierta para matriculas");
        }
    }

    private void validarChoqueEstudiante(Long estudianteId, Seccion nuevaSeccion) {
        List<Matricula> actuales = matriculaRepositorio.buscarActivasConHorarios(estudianteId,
                nuevaSeccion.getPeriodo().getId(), EstadoMatricula.ACTIVA);
        for (Matricula actual : actuales) {
            for (HorarioSeccion nuevo : nuevaSeccion.getHorarios()) {
                if (actual.getSeccion().getHorarios().stream().anyMatch(nuevo::seSuperpone)) {
                    throw new ConflictoException("La seccion tiene un choque de horario con "
                            + actual.getSeccion().getCodigo());
                }
            }
        }
    }

    private void validarPrerrequisitos(Long estudianteId, CursoValidacion curso) {
        if (curso.prerequisitoIds() == null || curso.prerequisitoIds().isEmpty()) {
            return;
        }
        ValidacionPrerrequisitos validacion = integracion.validarPrerrequisitos(
                estudianteId, curso.prerequisitoIds());
        if (!validacion.cumple()) {
            throw new ReglaNegocioException(
                    "El estudiante no ha aprobado los cursos prerrequisito: "
                            + validacion.cursosPendientes());
        }
    }

    private void exigirConsultaPermitida(Matricula matricula) {
        boolean administrador = contextoUsuario.tieneRol("ADMINISTRADOR");
        boolean propietario = contextoUsuario.usuarioId().equals(matricula.getEstudianteId());
        boolean docente = contextoUsuario.tieneRol("DOCENTE")
                && contextoUsuario.usuarioId().equals(matricula.getSeccion().getDocenteId());
        if (!administrador && !propietario && !docente) {
            throw new AccessDeniedException("No puede consultar esta matricula");
        }
    }

    private void validarActiva(Matricula matricula) {
        if (matricula.getEstado() != EstadoMatricula.ACTIVA) {
            throw new ReglaNegocioException("La matricula ya no esta activa");
        }
    }

    private Matricula buscar(Long id) {
        return matriculaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la matricula con id " + id));
    }

    private MatriculaRespuesta respuesta(Matricula matricula) {
        return new MatriculaRespuesta(matricula.getId(), matricula.getEstudianteId(),
                matricula.getSeccion().getId(), matricula.getSeccion().getCodigo(),
                matricula.getPeriodo().getId(), matricula.getPeriodo().getCodigo(),
                matricula.getCursoId(), matricula.getFechaMatricula(), matricula.getEstado(),
                matricula.getFechaRetiro(), matricula.getMotivoRetiro(), matricula.getCreadoEn(),
                matricula.getActualizadoEn());
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }
}
