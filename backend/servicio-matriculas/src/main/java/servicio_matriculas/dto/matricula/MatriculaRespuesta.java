package servicio_matriculas.dto.matricula;

import servicio_matriculas.dominio.EstadoMatricula;

import java.time.OffsetDateTime;

public record MatriculaRespuesta(Long id, Long estudianteId, Long seccionId, String seccionCodigo,
                                 Long periodoId, String periodoCodigo, Long cursoId,
                                 OffsetDateTime fechaMatricula, EstadoMatricula estado,
                                 OffsetDateTime fechaRetiro, String motivoRetiro,
                                 OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
}
