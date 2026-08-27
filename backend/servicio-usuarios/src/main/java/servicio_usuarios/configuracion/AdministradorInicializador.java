package servicio_usuarios.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;
import servicio_usuarios.repositorio.UsuarioRepositorio;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministradorInicializador implements ApplicationRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled}")
    private boolean habilitado;

    @Value("${app.bootstrap-admin.email}")
    private String correo;

    @Value("${app.bootstrap-admin.password}")
    private String clave;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (habilitado && !usuarioRepositorio.existsByCorreoIgnoreCase(correoNormalizado)) {
            usuarioRepositorio.save(new Usuario(correoNormalizado, passwordEncoder.encode(clave),
                    Set.of(RolUsuario.ADMINISTRADOR)));
            log.info("Administrador inicial creado para {}. Cambie ADMIN_PASSWORD fuera del entorno local.",
                    correoNormalizado);
        }
    }
}
