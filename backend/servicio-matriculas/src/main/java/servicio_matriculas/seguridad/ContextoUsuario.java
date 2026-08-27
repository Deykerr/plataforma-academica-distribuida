package servicio_matriculas.seguridad;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ContextoUsuario {

    public Long usuarioId() {
        Object valor = token().getToken().getClaim("usuarioId");
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        throw new AccessDeniedException("El token no contiene usuarioId");
    }

    public String bearerToken() {
        return "Bearer " + token().getToken().getTokenValue();
    }

    public boolean tieneRol(String rol) {
        return token().getAuthorities().stream().anyMatch(
                autoridad -> autoridad.getAuthority().equals("ROLE_" + rol));
    }

    public void exigirPropietarioOAdministrador(Long usuarioId) {
        if (!tieneRol("ADMINISTRADOR") && !usuarioId().equals(usuarioId)) {
            throw new AccessDeniedException("No puede operar sobre la matricula de otro estudiante");
        }
    }

    private JwtAuthenticationToken token() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt) {
            return jwt;
        }
        throw new AccessDeniedException("No existe una autenticacion JWT activa");
    }
}
