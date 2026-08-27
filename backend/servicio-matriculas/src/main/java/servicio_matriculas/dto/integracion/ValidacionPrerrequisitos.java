package servicio_matriculas.dto.integracion;

import java.util.Set;

public record ValidacionPrerrequisitos(Long estudianteId, boolean cumple,
                                       Set<Long> cursosAprobados,
                                       Set<Long> cursosPendientes) {
}
