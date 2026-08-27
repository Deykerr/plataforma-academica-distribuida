package servicio_matriculas.dto.seccion;

import jakarta.validation.constraints.NotNull;
import servicio_matriculas.dominio.EstadoSeccion;

public record CambiarEstadoSeccionSolicitud(@NotNull EstadoSeccion estado) {
}
