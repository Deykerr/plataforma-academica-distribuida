package servicio_matriculas.dto.reporte;

public record ResumenPeriodoRespuesta(Long periodoId, String periodoCodigo, long totalSecciones,
                                      long matriculasActivas, long estudiantesUnicos,
                                      long capacidadTotal, long vacantesDisponibles,
                                      double porcentajeOcupacion) {
}
