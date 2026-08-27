package servicio_evaluaciones.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_evaluaciones.dominio.Calificacion;
import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.Evaluacion;
import servicio_evaluaciones.dto.calificacion.*;
import servicio_evaluaciones.dto.integracion.MatriculaValidacion;
import servicio_evaluaciones.excepcion.ConflictoException;
import servicio_evaluaciones.excepcion.RecursoNoEncontradoException;
import servicio_evaluaciones.excepcion.ReglaNegocioException;
import servicio_evaluaciones.integracion.IntegracionMatriculasCliente;
import servicio_evaluaciones.repositorio.CalificacionRepositorio;
import servicio_evaluaciones.seguridad.ContextoUsuario;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalificacionServicio {
    private final CalificacionRepositorio calificacionRepositorio;
    private final EvaluacionServicio evaluacionServicio;
    private final IntegracionMatriculasCliente integracion;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public CalificacionRespuesta crear(CrearCalificacionSolicitud solicitud) {
        Evaluacion evaluacion = evaluacionServicio.buscar(solicitud.evaluacionId());
        validarGestion(evaluacion);
        if (calificacionRepositorio.existsByEvaluacionIdAndMatriculaId(
                evaluacion.getId(), solicitud.matriculaId())) {
            throw new ConflictoException("La matricula ya tiene una nota para esta evaluacion");
        }
        return respuesta(crearEntidad(evaluacion, solicitud.matriculaId(),
                solicitud.valor(), solicitud.observacion()));
    }

    @Transactional
    public List<CalificacionRespuesta> crearLote(RegistrarCalificacionesLoteSolicitud solicitud) {
        Evaluacion evaluacion = evaluacionServicio.buscar(solicitud.evaluacionId());
        validarGestion(evaluacion);
        Set<Long> matriculas = new HashSet<>();
        for (ItemCalificacionSolicitud item : solicitud.calificaciones()) {
            if (!matriculas.add(item.matriculaId())) {
                throw new ReglaNegocioException("El lote contiene una matricula repetida");
            }
            if (calificacionRepositorio.existsByEvaluacionIdAndMatriculaId(
                    evaluacion.getId(), item.matriculaId())) {
                throw new ConflictoException("La matricula " + item.matriculaId()
                        + " ya tiene una nota para esta evaluacion");
            }
        }
        return solicitud.calificaciones().stream()
                .map(item -> respuesta(crearEntidad(evaluacion, item.matriculaId(),
                        item.valor(), item.observacion())))
                .toList();
    }

    @Transactional
    public CalificacionRespuesta actualizar(Long id, ActualizarCalificacionSolicitud solicitud) {
        Calificacion calificacion = buscar(id);
        validarGestion(calificacion.getEvaluacion());
        validarValor(solicitud.valor(), calificacion.getEvaluacion().getNotaMaxima());
        calificacion.actualizar(solicitud.valor(), limpiarOpcional(solicitud.observacion()),
                contextoUsuario.usuarioId());
        return respuesta(calificacion);
    }

    @Transactional(readOnly = true)
    public CalificacionRespuesta obtener(Long id) {
        Calificacion calificacion = buscar(id);
        Evaluacion evaluacion = calificacion.getEvaluacion();
        boolean gestor = contextoUsuario.tieneRol("ADMINISTRADOR")
                || (contextoUsuario.tieneRol("DOCENTE")
                && contextoUsuario.usuarioId().equals(evaluacion.getDocenteId()));
        boolean propietario = contextoUsuario.tieneRol("ESTUDIANTE")
                && contextoUsuario.usuarioId().equals(calificacion.getEstudianteId())
                && Set.of(EstadoEvaluacion.PUBLICADA, EstadoEvaluacion.CERRADA)
                .contains(evaluacion.getEstado());
        if (!gestor && !propietario) throw new AccessDeniedException("No puede consultar esta nota");
        return respuesta(calificacion);
    }

    @Transactional(readOnly = true)
    public List<CalificacionRespuesta> listarPorEvaluacion(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionServicio.buscar(evaluacionId);
        contextoUsuario.exigirAdministradorODocente(evaluacion.getDocenteId());
        return calificacionRepositorio.findByEvaluacionIdOrderByEstudianteIdAsc(evaluacionId)
                .stream().map(this::respuesta).toList();
    }

    private Calificacion crearEntidad(Evaluacion evaluacion, Long matriculaId,
                                      BigDecimal valor, String observacion) {
        validarValor(valor, evaluacion.getNotaMaxima());
        MatriculaValidacion matricula = integracion.validarMatricula(matriculaId);
        if (!matricula.existe() || !Set.of("ACTIVA", "COMPLETADA").contains(matricula.estado())) {
            throw new ReglaNegocioException("La matricula no existe o no es valida para calificar");
        }
        if (!evaluacion.getSeccionId().equals(matricula.seccionId())) {
            throw new ReglaNegocioException("La matricula no pertenece a la seccion de la evaluacion");
        }
        Calificacion calificacion = new Calificacion(evaluacion, matriculaId,
                matricula.estudianteId(), valor, limpiarOpcional(observacion),
                contextoUsuario.usuarioId());
        return calificacionRepositorio.save(calificacion);
    }

    private void validarGestion(Evaluacion evaluacion) {
        contextoUsuario.exigirAdministradorODocente(evaluacion.getDocenteId());
        if (Set.of(EstadoEvaluacion.CERRADA, EstadoEvaluacion.ANULADA).contains(evaluacion.getEstado())) {
            throw new ReglaNegocioException("La evaluacion esta cerrada y sus notas no pueden modificarse");
        }
    }

    private void validarValor(BigDecimal valor, BigDecimal maximo) {
        if (valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(maximo) > 0) {
            throw new ReglaNegocioException("La nota debe estar entre 0 y " + maximo);
        }
    }

    private Calificacion buscar(Long id) {
        return calificacionRepositorio.findWithEvaluacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la calificacion con id " + id));
    }

    private CalificacionRespuesta respuesta(Calificacion c) {
        Evaluacion e = c.getEvaluacion();
        return new CalificacionRespuesta(c.getId(), e.getId(), e.getCodigo(), e.getNombre(),
                e.getTipo(), e.getEstado(), c.getMatriculaId(), c.getEstudianteId(), c.getValor(),
                e.getNotaMaxima(), e.getPonderacion(), c.getObservacion(), c.getRegistradoPor(),
                c.getCreadoEn(), c.getActualizadoEn());
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().replaceAll("\\s+", " ");
    }
}
