package servicio_usuarios.dto.usuario;

import jakarta.validation.constraints.NotEmpty;
import servicio_usuarios.dominio.RolUsuario;

import java.util.Set;

public record ActualizarRolesSolicitud(@NotEmpty Set<RolUsuario> roles) {
}
