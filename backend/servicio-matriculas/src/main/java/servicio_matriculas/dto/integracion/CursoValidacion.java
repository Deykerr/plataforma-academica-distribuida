package servicio_matriculas.dto.integracion;

import java.util.Set;

public record CursoValidacion(Long cursoId, boolean existe, boolean activo, Long carreraId,
                              Long cicloId, Integer creditos, Set<Long> prerequisitoIds) {
}
