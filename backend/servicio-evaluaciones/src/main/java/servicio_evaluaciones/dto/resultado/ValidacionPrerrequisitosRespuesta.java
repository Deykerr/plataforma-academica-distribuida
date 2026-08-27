package servicio_evaluaciones.dto.resultado;

import java.util.Set;

public record ValidacionPrerrequisitosRespuesta(Long estudianteId, boolean cumple,
                                                Set<Long> cursosAprobados,
                                                Set<Long> cursosPendientes) {
}
