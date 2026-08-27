package servicio_usuarios.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;
import servicio_usuarios.dto.usuario.UsuarioRespuesta;
import servicio_usuarios.dto.usuario.ValidacionUsuarioRespuesta;
import servicio_usuarios.excepcion.RecursoNoEncontradoException;
import servicio_usuarios.repositorio.UsuarioRepositorio;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional(readOnly = true)
    public UsuarioRespuesta obtener(Long id) {
        return UsuarioRespuesta.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public ValidacionUsuarioRespuesta validar(Long id, RolUsuario rolRequerido) {
        return usuarioRepositorio.findById(id)
                .map(usuario -> {
                    boolean cumpleRol = rolRequerido == null || usuario.getRoles().contains(rolRequerido);
                    return new ValidacionUsuarioRespuesta(usuario.getId(), true,
                            usuario.estaActivo() && cumpleRol, usuario.getEstado(),
                            Set.copyOf(usuario.getRoles()));
                })
                .orElseGet(() -> new ValidacionUsuarioRespuesta(id, false, false,
                        null, Collections.emptySet()));
    }

    @Transactional
    public UsuarioRespuesta cambiarEstado(Long id, EstadoUsuario estado) {
        Usuario usuario = buscar(id);
        usuario.cambiarEstado(estado);
        return UsuarioRespuesta.desde(usuario);
    }

    @Transactional
    public UsuarioRespuesta cambiarRoles(Long id, Set<RolUsuario> roles) {
        Usuario usuario = buscar(id);
        usuario.cambiarRoles(roles);
        return UsuarioRespuesta.desde(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }
}
