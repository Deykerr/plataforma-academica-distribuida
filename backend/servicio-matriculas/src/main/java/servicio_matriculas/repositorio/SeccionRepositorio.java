package servicio_matriculas.repositorio;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import servicio_matriculas.dominio.EstadoSeccion;
import servicio_matriculas.dominio.Seccion;

import java.util.List;
import java.util.Optional;

public interface SeccionRepositorio extends JpaRepository<Seccion, Long>, JpaSpecificationExecutor<Seccion> {
    boolean existsByPeriodoIdAndCodigoIgnoreCase(Long periodoId, String codigo);
    boolean existsByPeriodoIdAndCodigoIgnoreCaseAndIdNot(Long periodoId, String codigo, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seccion s where s.id = :id")
    Optional<Seccion> bloquearPorId(@Param("id") Long id);

    List<Seccion> findAllByPeriodoIdAndIdNotAndEstadoNot(Long periodoId, Long id, EstadoSeccion estado);
    List<Seccion> findAllByPeriodoIdAndEstadoNot(Long periodoId, EstadoSeccion estado);
}
