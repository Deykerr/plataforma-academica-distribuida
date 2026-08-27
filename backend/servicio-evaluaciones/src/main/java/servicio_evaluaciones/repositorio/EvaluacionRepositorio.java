package servicio_evaluaciones.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.Evaluacion;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface EvaluacionRepositorio extends JpaRepository<Evaluacion, Long>,
        JpaSpecificationExecutor<Evaluacion> {
    boolean existsBySeccionIdAndCodigoIgnoreCase(Long seccionId, String codigo);
    boolean existsBySeccionIdAndCodigoIgnoreCaseAndIdNot(Long seccionId, String codigo, Long id);

    @Query("select coalesce(sum(e.ponderacion), 0) from Evaluacion e " +
            "where e.seccionId = :seccionId and e.estado <> :estadoExcluido")
    BigDecimal sumarPonderacion(@Param("seccionId") Long seccionId,
                                @Param("estadoExcluido") EstadoEvaluacion estadoExcluido);

    @Query("select coalesce(sum(e.ponderacion), 0) from Evaluacion e " +
            "where e.seccionId = :seccionId and e.id <> :id and e.estado <> :estadoExcluido")
    BigDecimal sumarPonderacionExcluyendo(@Param("seccionId") Long seccionId,
                                          @Param("id") Long id,
                                          @Param("estadoExcluido") EstadoEvaluacion estadoExcluido);

    List<Evaluacion> findBySeccionIdAndEstadoInOrderByFechaAscIdAsc(
            Long seccionId, Collection<EstadoEvaluacion> estados);
}
