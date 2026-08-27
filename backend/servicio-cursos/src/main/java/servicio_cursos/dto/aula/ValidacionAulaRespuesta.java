package servicio_cursos.dto.aula;

import servicio_cursos.dominio.EstadoAula;
import servicio_cursos.dominio.TipoAula;

public record ValidacionAulaRespuesta(Long aulaId, boolean existe, boolean disponible,
                                     boolean aforoSuficiente, Integer capacidad,
                                     TipoAula tipo, EstadoAula estado) {
}
