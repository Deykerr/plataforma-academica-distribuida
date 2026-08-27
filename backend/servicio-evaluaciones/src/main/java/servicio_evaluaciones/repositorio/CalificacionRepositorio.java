package servicio_evaluaciones.repositorio;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import servicio_evaluaciones.dominio.Calificacion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface CalificacionRepositorio extends JpaRepository<Calificacion, Long> {
    boolean existsByEvaluacionIdAndMatriculaId(Long evaluacionId, Long matriculaId);
    boolean existsByEvaluacionId(Long evaluacionId);

    @Query("select max(c.valor) from Calificacion c where c.evaluacion.id = :evaluacionId")
    Optional<BigDecimal> buscarNotaMaximaRegistrada(@Param("evaluacionId") Long evaluacionId);

    @EntityGraph(attributePaths = "evaluacion")
    Optional<Calificacion> findWithEvaluacionById(Long id);

    @EntityGraph(attributePaths = "evaluacion")
    List<Calificacion> findByEvaluacionIdOrderByEstudianteIdAsc(Long evaluacionId);

    @EntityGraph(attributePaths = "evaluacion")
    List<Calificacion> findByMatriculaIdOrderByEvaluacionFechaAscEvaluacionIdAsc(Long matriculaId);

    @Query("select distinct c.matriculaId from Calificacion c " +
            "where c.evaluacion.seccionId = :seccionId and c.evaluacion.estado in :estados")
    List<Long> buscarMatriculasCalificadas(@Param("seccionId") Long seccionId,
                                            @Param("estados") Collection<?> estados);
}
