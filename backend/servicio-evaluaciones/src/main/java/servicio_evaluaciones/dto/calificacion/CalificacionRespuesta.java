package servicio_evaluaciones.dto.calificacion;

import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.TipoEvaluacion;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CalificacionRespuesta(Long id, Long evaluacionId, String evaluacionCodigo,
                                    String evaluacionNombre, TipoEvaluacion tipo,
                                    EstadoEvaluacion estadoEvaluacion, Long matriculaId,
                                    Long estudianteId, BigDecimal valor, BigDecimal notaMaxima,
                                    BigDecimal ponderacion, String observacion, Long registradoPor,
                                    OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
}
