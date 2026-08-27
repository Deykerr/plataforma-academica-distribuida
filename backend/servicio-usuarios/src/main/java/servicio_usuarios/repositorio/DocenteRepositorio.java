package servicio_usuarios.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import servicio_usuarios.dominio.Docente;

public interface DocenteRepositorio extends JpaRepository<Docente, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
    Page<Docente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCodigoContainingIgnoreCase(
            String nombres, String apellidos, String codigo, Pageable pageable);
}
