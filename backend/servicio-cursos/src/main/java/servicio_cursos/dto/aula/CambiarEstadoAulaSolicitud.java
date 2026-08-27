package servicio_cursos.dto.aula;

import jakarta.validation.constraints.NotNull;
import servicio_cursos.dominio.EstadoAula;

public record CambiarEstadoAulaSolicitud(@NotNull EstadoAula estado) {
}
