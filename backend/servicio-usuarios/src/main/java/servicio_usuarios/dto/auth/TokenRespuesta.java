package servicio_usuarios.dto.auth;

import servicio_usuarios.dominio.RolUsuario;

import java.util.Set;

public record TokenRespuesta(String token, String tipo, long expiraEnSegundos,
                             Long usuarioId, String correo, Set<RolUsuario> roles) {
}
