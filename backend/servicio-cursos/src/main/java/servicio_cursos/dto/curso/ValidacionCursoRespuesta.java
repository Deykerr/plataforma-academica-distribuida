package servicio_cursos.dto.curso;

import java.util.Set;

public record ValidacionCursoRespuesta(Long cursoId, boolean existe, boolean activo,
                                       Long carreraId, Long cicloId, Integer creditos,
                                       Set<Long> prerequisitoIds) {
}
