package servicio_usuarios.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_usuarios.dominio.Usuario;
import servicio_usuarios.dto.auth.LoginSolicitud;
import servicio_usuarios.dto.auth.TokenRespuesta;
import servicio_usuarios.excepcion.CredencialesInvalidasException;
import servicio_usuarios.repositorio.UsuarioRepositorio;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AutenticacionServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final TokenServicio tokenServicio;

    @Transactional(readOnly = true)
    public TokenRespuesta autenticar(LoginSolicitud solicitud) {
        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepositorio.findByCorreoIgnoreCase(correo)
                .filter(Usuario::estaActivo)
                .filter(u -> passwordEncoder.matches(solicitud.clave(), u.getClaveHash()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o clave incorrectos"));

        return new TokenRespuesta(tokenServicio.generar(usuario), "Bearer",
                tokenServicio.duracionEnSegundos(), usuario.getId(), usuario.getCorreo(),
                Set.copyOf(usuario.getRoles()));
    }
}
