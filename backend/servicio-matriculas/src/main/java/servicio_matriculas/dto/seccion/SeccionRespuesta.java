package servicio_matriculas.dto.seccion;

import servicio_matriculas.dominio.EstadoSeccion;

import java.time.OffsetDateTime;
import java.util.List;

public record SeccionRespuesta(Long id, Long periodoId, String periodoCodigo, Long cursoId,
                               Long aulaId, Long docenteId, String codigo, Integer capacidad,
                               long matriculados, long vacantesDisponibles, EstadoSeccion estado,
                               List<HorarioRespuesta> horarios, OffsetDateTime creadoEn,
                               OffsetDateTime actualizadoEn) {
}
