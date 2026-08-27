package servicio_evaluaciones.dto.historial;

import servicio_evaluaciones.dominio.EstadoResultado;
import java.math.BigDecimal;
import java.util.List;

public record HistorialAcademicoRespuesta(Long matriculaId, Long estudianteId,
                                          Long seccionId, Long periodoId, Long cursoId,
                                          BigDecimal ponderacionConfigurada,
                                          BigDecimal ponderacionEvaluada,
                                          BigDecimal promedioAcumulado,
                                          BigDecimal promedioSobreLoEvaluado,
                                          BigDecimal notaAprobatoria,
                                          EstadoResultado estadoFinal,
                                          List<DetalleNotaRespuesta> calificaciones) {
}
