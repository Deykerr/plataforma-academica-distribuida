package servicio_matriculas.dto.periodo;

import jakarta.validation.constraints.NotNull;
import servicio_matriculas.dominio.EstadoPeriodo;

public record CambiarEstadoPeriodoSolicitud(@NotNull EstadoPeriodo estado) {
}
