package servicio_matriculas.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import servicio_matriculas.dominio.EstadoMatricula;
import servicio_matriculas.dominio.Matricula;

import java.util.List;

public interface MatriculaRepositorio extends JpaRepository<Matricula, Long>, JpaSpecificationExecutor<Matricula> {
    long countBySeccionIdAndEstado(Long seccionId, EstadoMatricula estado);
    long countByPeriodoIdAndEstado(Long periodoId, EstadoMatricula estado);

    boolean existsByEstudianteIdAndSeccionIdAndEstado(Long estudianteId, Long seccionId,
                                                       EstadoMatricula estado);
    boolean existsByEstudianteIdAndPeriodoIdAndCursoIdAndEstado(Long estudianteId, Long periodoId,
                                                                 Long cursoId, EstadoMatricula estado);

    @Query("select distinct m from Matricula m join fetch m.seccion s left join fetch s.horarios " +
            "where m.estudianteId = :estudianteId and m.periodo.id = :periodoId and m.estado = :estado")
    List<Matricula> buscarActivasConHorarios(@Param("estudianteId") Long estudianteId,
                                              @Param("periodoId") Long periodoId,
                                              @Param("estado") EstadoMatricula estado);

    @Query("select count(distinct m.estudianteId) from Matricula m " +
            "where m.periodo.id = :periodoId and m.estado = :estado")
    long contarEstudiantesUnicos(@Param("periodoId") Long periodoId,
                                  @Param("estado") EstadoMatricula estado);
}
