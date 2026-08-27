package servicio_evaluaciones.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_evaluaciones.dominio.Calificacion;
import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.EstadoResultado;
import servicio_evaluaciones.dominio.Evaluacion;
import servicio_evaluaciones.dto.historial.DetalleNotaRespuesta;
import servicio_evaluaciones.dto.historial.HistorialAcademicoRespuesta;
import servicio_evaluaciones.dto.integracion.MatriculaValidacion;
import servicio_evaluaciones.dto.integracion.SeccionValidacion;
import servicio_evaluaciones.dto.reporte.ResumenSeccionRespuesta;
import servicio_evaluaciones.dto.resultado.ValidacionPrerrequisitosRespuesta;
import servicio_evaluaciones.dto.resultado.ValidarPrerrequisitosSolicitud;
import servicio_evaluaciones.excepcion.RecursoNoEncontradoException;
import servicio_evaluaciones.integracion.IntegracionMatriculasCliente;
import servicio_evaluaciones.repositorio.CalificacionRepositorio;
import servicio_evaluaciones.repositorio.EvaluacionRepositorio;
import servicio_evaluaciones.seguridad.ContextoUsuario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HistorialServicio {
    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal VEINTE = new BigDecimal("20.00");
    private static final Set<EstadoEvaluacion> ESTADOS_OFICIALES =
            Set.of(EstadoEvaluacion.PUBLICADA, EstadoEvaluacion.CERRADA);

    private final EvaluacionRepositorio evaluacionRepositorio;
    private final CalificacionRepositorio calificacionRepositorio;
    private final IntegracionMatriculasCliente integracion;
    private final ContextoUsuario contextoUsuario;
    private final BigDecimal notaAprobatoria;

    public HistorialServicio(EvaluacionRepositorio evaluacionRepositorio,
                             CalificacionRepositorio calificacionRepositorio,
                             IntegracionMatriculasCliente integracion,
                             ContextoUsuario contextoUsuario,
                             @Value("${app.evaluaciones.nota-aprobatoria}") BigDecimal notaAprobatoria) {
        this.evaluacionRepositorio = evaluacionRepositorio;
        this.calificacionRepositorio = calificacionRepositorio;
        this.integracion = integracion;
        this.contextoUsuario = contextoUsuario;
        this.notaAprobatoria = notaAprobatoria;
        if (notaAprobatoria.compareTo(BigDecimal.ZERO) < 0
                || notaAprobatoria.compareTo(VEINTE) > 0) {
            throw new IllegalArgumentException("NOTA_APROBATORIA debe estar entre 0 y 20");
        }
    }

    @Transactional(readOnly = true)
    public HistorialAcademicoRespuesta historial(Long matriculaId) {
        MatriculaValidacion matricula = obtenerMatricula(matriculaId);
        boolean administrador = contextoUsuario.tieneRol("ADMINISTRADOR");
        boolean propietario = contextoUsuario.tieneRol("ESTUDIANTE")
                && contextoUsuario.usuarioId().equals(matricula.estudianteId());
        if (!administrador && !propietario) {
            throw new AccessDeniedException("El historial solo puede verlo su titular o la administracion");
        }
        return construir(matricula);
    }

    @Transactional(readOnly = true)
    public List<HistorialAcademicoRespuesta> resultadosSeccion(Long seccionId) {
        SeccionValidacion seccion = integracion.obtenerSeccion(seccionId);
        contextoUsuario.exigirAdministradorODocente(seccion.docenteId());
        return calificacionRepositorio.buscarMatriculasCalificadas(seccionId, ESTADOS_OFICIALES)
                .stream().map(this::obtenerMatricula).map(this::construir).toList();
    }

    @Transactional(readOnly = true)
    public ResumenSeccionRespuesta resumenSeccion(Long seccionId) {
        SeccionValidacion seccion = integracion.obtenerSeccion(seccionId);
        contextoUsuario.exigirAdministradorODocente(seccion.docenteId());
        List<Evaluacion> evaluaciones = oficiales(seccionId);
        List<HistorialAcademicoRespuesta> resultados =
                calificacionRepositorio.buscarMatriculasCalificadas(seccionId, ESTADOS_OFICIALES)
                        .stream().map(this::obtenerMatricula).map(this::construir).toList();
        List<HistorialAcademicoRespuesta> completos = resultados.stream()
                .filter(r -> r.estadoFinal() != EstadoResultado.EN_PROCESO).toList();
        BigDecimal promedio = completos.isEmpty() ? BigDecimal.ZERO.setScale(2)
                : completos.stream().map(HistorialAcademicoRespuesta::promedioAcumulado)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completos.size()), 2, RoundingMode.HALF_UP);
        int aprobados = (int) completos.stream()
                .filter(r -> r.estadoFinal() == EstadoResultado.APROBADO).count();
        return new ResumenSeccionRespuesta(seccionId, seccion.periodoId(), seccion.cursoId(),
                evaluaciones.size(), sumaPonderacion(evaluaciones), resultados.size(), completos.size(),
                aprobados, completos.size() - aprobados, promedio);
    }

    @Transactional(readOnly = true)
    public ValidacionPrerrequisitosRespuesta validarPrerrequisitos(
            ValidarPrerrequisitosSolicitud solicitud) {
        boolean administrador = contextoUsuario.tieneRol("ADMINISTRADOR");
        boolean propietario = contextoUsuario.tieneRol("ESTUDIANTE")
                && contextoUsuario.usuarioId().equals(solicitud.estudianteId());
        if (!administrador && !propietario) {
            throw new AccessDeniedException(
                    "Solo el estudiante titular o la administracion pueden validar prerrequisitos");
        }

        Set<Long> aprobados = new LinkedHashSet<>();
        Set<Long> pendientes = new LinkedHashSet<>();
        for (Long cursoId : solicitud.cursoIds()) {
            boolean aprobado = calificacionRepositorio.buscarMatriculasPorEstudianteYCurso(
                            solicitud.estudianteId(), cursoId, ESTADOS_OFICIALES)
                    .stream().map(this::obtenerMatricula).map(this::construir)
                    .anyMatch(resultado -> resultado.estadoFinal() == EstadoResultado.APROBADO);
            if (aprobado) aprobados.add(cursoId); else pendientes.add(cursoId);
        }
        return new ValidacionPrerrequisitosRespuesta(solicitud.estudianteId(), pendientes.isEmpty(),
                Set.copyOf(aprobados), Set.copyOf(pendientes));
    }

    private HistorialAcademicoRespuesta construir(MatriculaValidacion matricula) {
        List<Evaluacion> evaluaciones = oficiales(matricula.seccionId());
        Map<Long, Calificacion> notas = calificacionRepositorio
                .findByMatriculaIdOrderByEvaluacionFechaAscEvaluacionIdAsc(matricula.matriculaId())
                .stream().filter(c -> ESTADOS_OFICIALES.contains(c.getEvaluacion().getEstado()))
                .collect(Collectors.toMap(c -> c.getEvaluacion().getId(), Function.identity()));

        BigDecimal configurada = sumaPonderacion(evaluaciones);
        BigDecimal evaluada = BigDecimal.ZERO;
        BigDecimal acumulado = BigDecimal.ZERO;
        java.util.ArrayList<DetalleNotaRespuesta> detalles = new java.util.ArrayList<>();
        for (Evaluacion evaluacion : evaluaciones) {
            Calificacion nota = notas.get(evaluacion.getId());
            BigDecimal aporte = BigDecimal.ZERO.setScale(2);
            if (nota != null) {
                evaluada = evaluada.add(evaluacion.getPonderacion());
                aporte = nota.getValor().divide(evaluacion.getNotaMaxima(), 8, RoundingMode.HALF_UP)
                        .multiply(VEINTE).multiply(evaluacion.getPonderacion())
                        .divide(CIEN, 2, RoundingMode.HALF_UP);
                acumulado = acumulado.add(aporte);
            }
            detalles.add(new DetalleNotaRespuesta(evaluacion.getId(), evaluacion.getCodigo(),
                    evaluacion.getNombre(), evaluacion.getTipo(), evaluacion.getPonderacion(),
                    evaluacion.getNotaMaxima(), nota == null ? null : nota.getValor(), aporte,
                    nota == null ? null : nota.getObservacion()));
        }

        acumulado = acumulado.setScale(2, RoundingMode.HALF_UP);
        BigDecimal promedioEvaluado = evaluada.signum() == 0 ? BigDecimal.ZERO.setScale(2)
                : acumulado.multiply(CIEN).divide(evaluada, 2, RoundingMode.HALF_UP);
        boolean completo = configurada.compareTo(CIEN) == 0 && evaluada.compareTo(CIEN) == 0;
        EstadoResultado estado = !completo ? EstadoResultado.EN_PROCESO
                : acumulado.compareTo(notaAprobatoria) >= 0
                ? EstadoResultado.APROBADO : EstadoResultado.DESAPROBADO;
        return new HistorialAcademicoRespuesta(matricula.matriculaId(), matricula.estudianteId(),
                matricula.seccionId(), matricula.periodoId(), matricula.cursoId(),
                configurada, evaluada, acumulado, promedioEvaluado,
                notaAprobatoria.setScale(2, RoundingMode.HALF_UP), estado, List.copyOf(detalles));
    }

    private MatriculaValidacion obtenerMatricula(Long id) {
        MatriculaValidacion matricula = integracion.validarMatricula(id);
        if (!matricula.existe()) {
            throw new RecursoNoEncontradoException("No existe la matricula con id " + id);
        }
        return matricula;
    }

    private List<Evaluacion> oficiales(Long seccionId) {
        return evaluacionRepositorio.findBySeccionIdAndEstadoInOrderByFechaAscIdAsc(
                seccionId, ESTADOS_OFICIALES);
    }

    private BigDecimal sumaPonderacion(List<Evaluacion> evaluaciones) {
        return evaluaciones.stream().map(Evaluacion::getPonderacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
