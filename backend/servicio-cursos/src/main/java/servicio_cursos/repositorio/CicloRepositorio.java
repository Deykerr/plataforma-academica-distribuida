package servicio_cursos.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import servicio_cursos.dominio.Ciclo;
import servicio_cursos.dominio.EstadoRegistro;

import java.util.List;

public interface CicloRepositorio extends JpaRepository<Ciclo, Long>, JpaSpecificationExecutor<Ciclo> {
    boolean existsByCarreraIdAndNumero(Long carreraId, Integer numero);
    boolean existsByCarreraIdAndNumeroAndIdNot(Long carreraId, Integer numero, Long id);
    List<Ciclo> findAllByCarreraIdAndEstadoOrderByNumeroAsc(Long carreraId, EstadoRegistro estado);
}
