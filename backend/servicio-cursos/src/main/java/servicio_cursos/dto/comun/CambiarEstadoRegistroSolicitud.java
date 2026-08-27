package servicio_cursos.dto.comun;

import jakarta.validation.constraints.NotNull;
import servicio_cursos.dominio.EstadoRegistro;

public record CambiarEstadoRegistroSolicitud(@NotNull EstadoRegistro estado) {
}
