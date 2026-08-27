package servicio_cursos.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import servicio_cursos.dominio.Aula;

public interface AulaRepositorio extends JpaRepository<Aula, Long>, JpaSpecificationExecutor<Aula> {
    boolean existsByCodigoIgnoreCase(String codigo);
}
