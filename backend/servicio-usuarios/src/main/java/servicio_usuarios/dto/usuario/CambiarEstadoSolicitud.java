package servicio_usuarios.dto.usuario;

import jakarta.validation.constraints.NotNull;
import servicio_usuarios.dominio.EstadoUsuario;

public record CambiarEstadoSolicitud(@NotNull EstadoUsuario estado) {
}
