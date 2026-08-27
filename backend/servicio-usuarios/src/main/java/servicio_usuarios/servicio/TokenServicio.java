package servicio_usuarios.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenServicio {

    private final JwtEncoder jwtEncoder;
    private final Duration duracion;

    public TokenServicio(JwtEncoder jwtEncoder,
                         @Value("${app.security.jwt.expiration-minutes}") long minutos) {
        this.jwtEncoder = jwtEncoder;
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        List<String> roles = usuario.getRoles().stream().map(RolUsuario::name).sorted().toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("servicio-usuarios")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .subject(usuario.getCorreo())
                .claim("usuarioId", usuario.getId())
                .claim("roles", roles)
                .build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }

    public long duracionEnSegundos() {
        return duracion.toSeconds();
    }
}
