package servicio_cursos.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import servicio_cursos.dominio.Curso;
import servicio_cursos.dominio.EstadoRegistro;

import java.util.List;

public interface CursoRepositorio extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Curso> findAllByCicloIdAndEstadoOrderByNombreAsc(Long cicloId, EstadoRegistro estado);
    List<Curso> findAllByPrerequisitosId(Long prerequisitoId);
}
