package servicio_matriculas.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import servicio_matriculas.dominio.EstadoPeriodo;
import servicio_matriculas.dominio.Periodo;

public interface PeriodoRepositorio extends JpaRepository<Periodo, Long>, JpaSpecificationExecutor<Periodo> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    boolean existsByEstadoAndIdNot(EstadoPeriodo estado, Long id);
}
