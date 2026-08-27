package servicio_evaluaciones.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.Evaluacion;
import servicio_evaluaciones.dominio.TipoEvaluacion;
import servicio_evaluaciones.dto.comun.PaginaRespuesta;
import servicio_evaluaciones.dto.evaluacion.ActualizarEvaluacionSolicitud;
import servicio_evaluaciones.dto.evaluacion.CrearEvaluacionSolicitud;
import servicio_evaluaciones.dto.evaluacion.EvaluacionRespuesta;
import servicio_evaluaciones.dto.integracion.SeccionValidacion;
import servicio_evaluaciones.excepcion.ConflictoException;
import servicio_evaluaciones.excepcion.RecursoNoEncontradoException;
import servicio_evaluaciones.excepcion.ReglaNegocioException;
import servicio_evaluaciones.integracion.IntegracionMatriculasCliente;
import servicio_evaluaciones.repositorio.CalificacionRepositorio;
import servicio_evaluaciones.repositorio.EvaluacionRepositorio;
import servicio_evaluaciones.seguridad.ContextoUsuario;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EvaluacionServicio {
    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final EvaluacionRepositorio evaluacionRepositorio;
    private final CalificacionRepositorio calificacionRepositorio;
    private final IntegracionMatriculasCliente integracion;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public EvaluacionRespuesta crear(CrearEvaluacionSolicitud solicitud) {
        SeccionValidacion seccion = integracion.obtenerSeccion(solicitud.seccionId());
        validarSeccion(seccion);
        contextoUsuario.exigirAdministradorODocente(seccion.docenteId());
        String codigo = mayusculas(solicitud.codigo());
        if (evaluacionRepositorio.existsBySeccionIdAndCodigoIgnoreCase(seccion.id(), codigo)) {
            throw new ConflictoException("El codigo de evaluacion ya existe en la seccion");
        }
        validarPonderacion(seccion.id(), null, solicitud.ponderacion());
        Evaluacion evaluacion = new Evaluacion(seccion.id(), seccion.periodoId(), seccion.cursoId(),
                seccion.docenteId(), codigo, limpiar(solicitud.nombre()), solicitud.tipo(),
                solicitud.ponderacion(), solicitud.notaMaxima(), solicitud.fecha());
        return respuesta(evaluacionRepositorio.save(evaluacion));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<EvaluacionRespuesta> listar(Long seccionId, Long periodoId,
                                                        EstadoEvaluacion estado,
                                                        TipoEvaluacion tipo, Pageable pageable) {
        Specification<Evaluacion> spec = (root, query, cb) -> cb.conjunction();
        if (seccionId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("seccionId"), seccionId));
        if (periodoId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("periodoId"), periodoId));
        if (estado != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("estado"), estado));
        if (tipo != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("tipo"), tipo));
        if (!contextoUsuario.tieneRol("ADMINISTRADOR")) {
            Long docenteId = contextoUsuario.usuarioId();
            spec = spec.and((r, q, cb) -> cb.equal(r.get("docenteId"), docenteId));
        }
        return PaginaRespuesta.desde(evaluacionRepositorio.findAll(spec, pageable).map(this::respuesta));
    }

    @Transactional(readOnly = true)
    public EvaluacionRespuesta obtener(Long id) {
        Evaluacion evaluacion = buscar(id);
        contextoUsuario.exigirAdministradorODocente(evaluacion.getDocenteId());
        return respuesta(evaluacion);
    }

    @Transactional
    public EvaluacionRespuesta actualizar(Long id, ActualizarEvaluacionSolicitud solicitud) {
        Evaluacion evaluacion = buscar(id);
        contextoUsuario.exigirAdministradorODocente(evaluacion.getDocenteId());
        if (evaluacion.getEstado() != EstadoEvaluacion.BORRADOR) {
            throw new ReglaNegocioException("Solo se puede editar una evaluacion en borrador");
        }
        String codigo = mayusculas(solicitud.codigo());
        if (evaluacionRepositorio.existsBySeccionIdAndCodigoIgnoreCaseAndIdNot(
                evaluacion.getSeccionId(), codigo, id)) {
            throw new ConflictoException("El codigo de evaluacion ya existe en la seccion");
        }
        validarPonderacion(evaluacion.getSeccionId(), id, solicitud.ponderacion());
        calificacionRepositorio.buscarNotaMaximaRegistrada(id).ifPresent(maxima -> {
            if (maxima.compareTo(solicitud.notaMaxima()) > 0) {
                throw new ReglaNegocioException(
                        "La nueva nota maxima es menor que una calificacion ya registrada");
            }
        });
        evaluacion.actualizar(codigo, limpiar(solicitud.nombre()), solicitud.tipo(),
                solicitud.ponderacion(), solicitud.notaMaxima(), solicitud.fecha());
        return respuesta(evaluacion);
    }

    @Transactional
    public EvaluacionRespuesta cambiarEstado(Long id, EstadoEvaluacion nuevoEstado) {
        Evaluacion evaluacion = buscar(id);
        contextoUsuario.exigirAdministradorODocente(evaluacion.getDocenteId());
        if (evaluacion.getEstado() == nuevoEstado) return respuesta(evaluacion);
        boolean valida = switch (evaluacion.getEstado()) {
            case BORRADOR -> Set.of(EstadoEvaluacion.PUBLICADA, EstadoEvaluacion.ANULADA).contains(nuevoEstado);
            case PUBLICADA -> Set.of(EstadoEvaluacion.CERRADA, EstadoEvaluacion.ANULADA).contains(nuevoEstado);
            case CERRADA, ANULADA -> false;
        };
        if (!valida) throw new ReglaNegocioException("No se puede cambiar la evaluacion de "
                + evaluacion.getEstado() + " a " + nuevoEstado);
        evaluacion.cambiarEstado(nuevoEstado);
        return respuesta(evaluacion);
    }

    @Transactional(readOnly = true)
    public Evaluacion buscar(Long id) {
        return evaluacionRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la evaluacion con id " + id));
    }

    private void validarSeccion(SeccionValidacion seccion) {
        if (seccion == null || seccion.id() == null) {
            throw new ReglaNegocioException("La seccion no existe");
        }
        if (Set.of("CANCELADA", "FINALIZADA").contains(seccion.estado())) {
            throw new ReglaNegocioException("No se pueden configurar evaluaciones en una seccion cerrada");
        }
    }

    private void validarPonderacion(Long seccionId, Long excluirId, BigDecimal nueva) {
        BigDecimal actual = excluirId == null
                ? evaluacionRepositorio.sumarPonderacion(seccionId, EstadoEvaluacion.ANULADA)
                : evaluacionRepositorio.sumarPonderacionExcluyendo(seccionId, excluirId,
                        EstadoEvaluacion.ANULADA);
        if (actual.add(nueva).compareTo(CIEN) > 0) {
            throw new ReglaNegocioException("La ponderacion total de la seccion no puede superar 100%");
        }
    }

    private EvaluacionRespuesta respuesta(Evaluacion e) {
        return new EvaluacionRespuesta(e.getId(), e.getSeccionId(), e.getPeriodoId(), e.getCursoId(),
                e.getDocenteId(), e.getCodigo(), e.getNombre(), e.getTipo(), e.getPonderacion(),
                e.getNotaMaxima(), e.getFecha(), e.getEstado(),
                evaluacionRepositorio.sumarPonderacion(e.getSeccionId(), EstadoEvaluacion.ANULADA),
                e.getCreadoEn(), e.getActualizadoEn());
    }

    private String limpiar(String valor) { return valor.trim().replaceAll("\\s+", " "); }
    private String mayusculas(String valor) { return limpiar(valor).toUpperCase(Locale.ROOT); }
}
