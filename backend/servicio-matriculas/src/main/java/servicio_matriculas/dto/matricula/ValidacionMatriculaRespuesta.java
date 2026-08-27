package servicio_matriculas.dto.matricula;

import servicio_matriculas.dominio.EstadoMatricula;

public record ValidacionMatriculaRespuesta(Long matriculaId, boolean existe, boolean activa,
                                           EstadoMatricula estado, Long estudianteId,
                                           Long seccionId, Long periodoId, Long cursoId) {
}
