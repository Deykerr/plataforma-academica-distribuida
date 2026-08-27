package servicio_cursos.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import servicio_cursos.dominio.Carrera;
import servicio_cursos.dominio.EstadoRegistro;

import java.util.List;

public interface CarreraRepositorio extends JpaRepository<Carrera, Long>, JpaSpecificationExecutor<Carrera> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    List<Carrera> findAllByEstadoOrderByNombreAsc(EstadoRegistro estado);
}
