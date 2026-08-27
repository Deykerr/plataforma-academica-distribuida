package servicio_usuarios.dto.usuario;

import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.RolUsuario;

import java.util.Set;

public record ValidacionUsuarioRespuesta(Long usuarioId, boolean existe, boolean activo,
                                         EstadoUsuario estado, Set<RolUsuario> roles) {
}
