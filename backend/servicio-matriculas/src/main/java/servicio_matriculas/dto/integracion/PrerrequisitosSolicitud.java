package servicio_matriculas.dto.integracion;

import java.util.Set;

public record PrerrequisitosSolicitud(Long estudianteId, Set<Long> cursoIds) {
}
