package servicio_usuarios.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import servicio_usuarios.dominio.Estudiante;

import java.util.Optional;

public interface EstudianteRepositorio extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByUsuarioCorreoIgnoreCase(String correo);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
    Page<Estudiante> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCodigoContainingIgnoreCase(
            String nombres, String apellidos, String codigo, Pageable pageable);
}
