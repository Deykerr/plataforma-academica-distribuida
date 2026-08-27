package servicio_evaluaciones.dto.evaluacion;

import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.TipoEvaluacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EvaluacionRespuesta(Long id, Long seccionId, Long periodoId, Long cursoId,
                                  Long docenteId, String codigo, String nombre,
                                  TipoEvaluacion tipo, BigDecimal ponderacion,
                                  BigDecimal notaMaxima, LocalDate fecha,
                                  EstadoEvaluacion estado, BigDecimal ponderacionTotalSeccion,
                                  OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
}
