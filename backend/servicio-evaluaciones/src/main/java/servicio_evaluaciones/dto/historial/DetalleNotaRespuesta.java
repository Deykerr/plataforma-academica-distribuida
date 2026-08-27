package servicio_evaluaciones.dto.historial;

import servicio_evaluaciones.dominio.TipoEvaluacion;
import java.math.BigDecimal;

public record DetalleNotaRespuesta(Long evaluacionId, String codigo, String nombre,
                                   TipoEvaluacion tipo, BigDecimal ponderacion,
                                   BigDecimal notaMaxima, BigDecimal nota,
                                   BigDecimal aportePonderado, String observacion) {
}
