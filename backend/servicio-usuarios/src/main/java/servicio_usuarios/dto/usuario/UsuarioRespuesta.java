package servicio_usuarios.dto.usuario;

import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;

import java.time.OffsetDateTime;
import java.util.Set;

public record UsuarioRespuesta(Long id, String correo, Set<RolUsuario> roles, EstadoUsuario estado,
                               OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getId(), usuario.getCorreo(), Set.copyOf(usuario.getRoles()),
                usuario.getEstado(), usuario.getCreadoEn(), usuario.getActualizadoEn());
    }
}
