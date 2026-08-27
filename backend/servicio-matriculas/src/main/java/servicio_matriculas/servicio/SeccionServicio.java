package servicio_matriculas.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_matriculas.dominio.EstadoMatricula;
import servicio_matriculas.dominio.EstadoPeriodo;
import servicio_matriculas.dominio.EstadoSeccion;
import servicio_matriculas.dominio.HorarioSeccion;
import servicio_matriculas.dominio.Periodo;
import servicio_matriculas.dominio.Seccion;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.integracion.AulaValidacion;
import servicio_matriculas.dto.integracion.CursoValidacion;
import servicio_matriculas.dto.integracion.UsuarioValidacion;
import servicio_matriculas.dto.seccion.ActualizarSeccionSolicitud;
import servicio_matriculas.dto.seccion.CrearSeccionSolicitud;
import servicio_matriculas.dto.seccion.HorarioRespuesta;
import servicio_matriculas.dto.seccion.HorarioSolicitud;
import servicio_matriculas.dto.seccion.SeccionRespuesta;
import servicio_matriculas.excepcion.ConflictoException;
import servicio_matriculas.excepcion.RecursoNoEncontradoException;
import servicio_matriculas.excepcion.ReglaNegocioException;
import servicio_matriculas.integracion.IntegracionAcademicaCliente;
import servicio_matriculas.repositorio.MatriculaRepositorio;
import servicio_matriculas.repositorio.SeccionRepositorio;
import servicio_matriculas.seguridad.ContextoUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeccionServicio {

    private final SeccionRepositorio seccionRepositorio;
    private final MatriculaRepositorio matriculaRepositorio;
    private final PeriodoServicio periodoServicio;
    private final IntegracionAcademicaCliente integracion;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public SeccionRespuesta crear(CrearSeccionSolicitud solicitud) {
        Periodo periodo = periodoServicio.buscarEntidad(solicitud.periodoId());
        validarPeriodoEditable(periodo);
        String codigo = mayusculas(solicitud.codigo());
        if (seccionRepositorio.existsByPeriodoIdAndCodigoIgnoreCase(periodo.getId(), codigo)) {
            throw new ConflictoException("El codigo de seccion ya existe en el periodo");
        }
        validarReferencias(solicitud.cursoId(), solicitud.aulaId(), solicitud.docenteId(),
                solicitud.capacidad());
        List<HorarioSeccion> horarios = convertirYValidar(solicitud.horarios());
        validarConflictos(periodo.getId(), -1L, solicitud.aulaId(), solicitud.docenteId(), horarios);

        Seccion seccion = new Seccion(periodo, solicitud.cursoId(), solicitud.aulaId(),
                solicitud.docenteId(), codigo, solicitud.capacidad());
        seccion.reemplazarHorarios(horarios);
        return respuesta(seccionRepositorio.save(seccion));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<SeccionRespuesta> listar(Long periodoId, Long cursoId, Long docenteId,
                                                     EstadoSeccion estado, Pageable pageable) {
        Specification<Seccion> spec = (root, query, cb) -> cb.conjunction();
        if (periodoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("periodo").get("id"), periodoId));
        }
        if (cursoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cursoId"), cursoId));
        }
        if (docenteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("docenteId"), docenteId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(seccionRepositorio.findAll(spec, pageable).map(this::respuesta));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<SeccionRespuesta> misSecciones(Long periodoId, Pageable pageable) {
        Long docenteId = contextoUsuario.usuarioId();
        return listar(periodoId, null, docenteId, null, pageable);
    }

    @Transactional(readOnly = true)
    public SeccionRespuesta obtener(Long id) {
        return respuesta(buscarEntidad(id));
    }

    @Transactional
    public SeccionRespuesta actualizar(Long id, ActualizarSeccionSolicitud solicitud) {
        Seccion seccion = buscarEntidad(id);
        if (!Set.of(EstadoSeccion.PLANIFICADA, EstadoSeccion.CERRADA).contains(seccion.getEstado())) {
            throw new ReglaNegocioException("Solo se puede editar una seccion planificada o cerrada");
        }
        if (ocupados(id) > 0) {
            throw new ConflictoException("No se puede reconfigurar una seccion con matriculas activas");
        }
        Periodo periodo = periodoServicio.buscarEntidad(solicitud.periodoId());
        validarPeriodoEditable(periodo);
        String codigo = mayusculas(solicitud.codigo());
        if (seccionRepositorio.existsByPeriodoIdAndCodigoIgnoreCaseAndIdNot(periodo.getId(), codigo, id)) {
            throw new ConflictoException("El codigo de seccion ya existe en el periodo");
        }
        validarReferencias(solicitud.cursoId(), solicitud.aulaId(), solicitud.docenteId(),
                solicitud.capacidad());
        List<HorarioSeccion> horarios = convertirYValidar(solicitud.horarios());
        validarConflictos(periodo.getId(), id, solicitud.aulaId(), solicitud.docenteId(), horarios);
        seccion.actualizar(periodo, solicitud.cursoId(), solicitud.aulaId(), solicitud.docenteId(),
                codigo, solicitud.capacidad());
        seccion.reemplazarHorarios(horarios);
        return respuesta(seccion);
    }

    @Transactional
    public SeccionRespuesta cambiarEstado(Long id, EstadoSeccion nuevoEstado) {
        Seccion seccion = buscarEntidad(id);
        if (seccion.getEstado() == nuevoEstado) {
            return respuesta(seccion);
        }
        validarTransicion(seccion.getEstado(), nuevoEstado);
        if (nuevoEstado == EstadoSeccion.ABIERTA
                && seccion.getPeriodo().getEstado() != EstadoPeriodo.MATRICULA_ABIERTA) {
            throw new ReglaNegocioException("La seccion solo puede abrirse durante la matricula del periodo");
        }
        if (nuevoEstado == EstadoSeccion.CANCELADA && ocupados(id) > 0) {
            throw new ConflictoException("Retire o anule las matriculas antes de cancelar la seccion");
        }
        seccion.cambiarEstado(nuevoEstado);
        return respuesta(seccion);
    }

    @Transactional(readOnly = true)
    public Seccion buscarEntidad(Long id) {
        return seccionRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la seccion con id " + id));
    }

    @Transactional
    public Seccion bloquear(Long id) {
        return seccionRepositorio.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la seccion con id " + id));
    }

    public long ocupados(Long seccionId) {
        return matriculaRepositorio.countBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA);
    }

    public SeccionRespuesta respuesta(Seccion seccion) {
        long matriculados = ocupados(seccion.getId());
        List<HorarioRespuesta> horarios = seccion.getHorarios().stream()
                .map(h -> new HorarioRespuesta(h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin()))
                .toList();
        return new SeccionRespuesta(seccion.getId(), seccion.getPeriodo().getId(),
                seccion.getPeriodo().getCodigo(), seccion.getCursoId(), seccion.getAulaId(),
                seccion.getDocenteId(), seccion.getCodigo(), seccion.getCapacidad(), matriculados,
                Math.max(0, seccion.getCapacidad() - matriculados), seccion.getEstado(), horarios,
                seccion.getCreadoEn(), seccion.getActualizadoEn());
    }

    private void validarPeriodoEditable(Periodo periodo) {
        if (Set.of(EstadoPeriodo.EN_CURSO, EstadoPeriodo.FINALIZADO,
                EstadoPeriodo.CANCELADO).contains(periodo.getEstado())) {
            throw new ReglaNegocioException("No se pueden configurar secciones en este estado del periodo");
        }
    }

    private void validarReferencias(Long cursoId, Long aulaId, Long docenteId, Integer capacidad) {
        CursoValidacion curso = integracion.validarCurso(cursoId);
        if (!curso.existe() || !curso.activo()) {
            throw new ReglaNegocioException("El curso no existe o no esta activo");
        }
        AulaValidacion aula = integracion.validarAula(aulaId, capacidad);
        if (!aula.existe() || !aula.disponible() || !aula.aforoSuficiente()) {
            throw new ReglaNegocioException("El aula no esta disponible o no tiene aforo suficiente");
        }
        UsuarioValidacion docente = integracion.validarUsuario(docenteId, "DOCENTE");
        if (!docente.existe() || !docente.activo()) {
            throw new ReglaNegocioException("El docente no existe, esta inactivo o no tiene ese rol");
        }
    }

    private List<HorarioSeccion> convertirYValidar(List<HorarioSolicitud> solicitudes) {
        List<HorarioSeccion> horarios = new ArrayList<>();
        for (HorarioSolicitud solicitud : solicitudes) {
            if (!solicitud.horaInicio().isBefore(solicitud.horaFin())) {
                throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin");
            }
            HorarioSeccion nuevo = new HorarioSeccion(solicitud.diaSemana(), solicitud.horaInicio(),
                    solicitud.horaFin());
            if (horarios.stream().anyMatch(nuevo::seSuperpone)) {
                throw new ReglaNegocioException("Los horarios de una seccion no pueden superponerse");
            }
            horarios.add(nuevo);
        }
        return horarios;
    }

    private void validarConflictos(Long periodoId, Long seccionId, Long aulaId, Long docenteId,
                                   List<HorarioSeccion> horarios) {
        for (Seccion existente : seccionRepositorio.findAllByPeriodoIdAndIdNotAndEstadoNot(
                periodoId, seccionId, EstadoSeccion.CANCELADA)) {
            if (!existente.getAulaId().equals(aulaId) && !existente.getDocenteId().equals(docenteId)) {
                continue;
            }
            boolean choque = horarios.stream().anyMatch(nuevo -> existente.getHorarios().stream()
                    .anyMatch(nuevo::seSuperpone));
            if (choque) {
                String recurso = existente.getAulaId().equals(aulaId) ? "aula" : "docente";
                throw new ConflictoException("Existe un choque de horario para el " + recurso
                        + " con la seccion " + existente.getCodigo());
            }
        }
    }

    private void validarTransicion(EstadoSeccion actual, EstadoSeccion nuevo) {
        boolean valida = switch (actual) {
            case PLANIFICADA -> Set.of(EstadoSeccion.ABIERTA, EstadoSeccion.CANCELADA).contains(nuevo);
            case ABIERTA -> Set.of(EstadoSeccion.CERRADA, EstadoSeccion.EN_CURSO,
                    EstadoSeccion.CANCELADA).contains(nuevo);
            case CERRADA -> Set.of(EstadoSeccion.ABIERTA, EstadoSeccion.EN_CURSO,
                    EstadoSeccion.CANCELADA).contains(nuevo);
            case EN_CURSO -> Set.of(EstadoSeccion.FINALIZADA, EstadoSeccion.CANCELADA).contains(nuevo);
            case FINALIZADA, CANCELADA -> false;
        };
        if (!valida) {
            throw new ReglaNegocioException("No se puede cambiar la seccion de " + actual + " a " + nuevo);
        }
    }

    private String mayusculas(String valor) {
        return valor.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
