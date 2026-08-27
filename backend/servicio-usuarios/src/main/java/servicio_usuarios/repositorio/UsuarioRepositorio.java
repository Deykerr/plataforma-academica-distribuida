package servicio_usuarios.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import servicio_usuarios.dominio.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
}
