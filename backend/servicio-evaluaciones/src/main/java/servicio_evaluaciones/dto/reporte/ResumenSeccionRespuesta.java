package servicio_evaluaciones.dto.reporte;

import java.math.BigDecimal;

public record ResumenSeccionRespuesta(Long seccionId, Long periodoId, Long cursoId,
                                      int evaluacionesOficiales, BigDecimal ponderacionConfigurada,
                                      int matriculasCalificadas, int resultadosCompletos,
                                      int aprobados, int desaprobados,
                                      BigDecimal promedioGeneral) {
}
