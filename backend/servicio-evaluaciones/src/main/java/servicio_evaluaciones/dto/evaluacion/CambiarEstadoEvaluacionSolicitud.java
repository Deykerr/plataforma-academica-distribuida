package servicio_evaluaciones.dto.evaluacion;

import jakarta.validation.constraints.NotNull;
import servicio_evaluaciones.dominio.EstadoEvaluacion;

public record CambiarEstadoEvaluacionSolicitud(@NotNull EstadoEvaluacion estado) {
}
