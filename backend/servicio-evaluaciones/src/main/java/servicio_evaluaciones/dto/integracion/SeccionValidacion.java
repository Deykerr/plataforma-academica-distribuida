package servicio_evaluaciones.dto.integracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeccionValidacion(Long id, Long periodoId, String periodoCodigo, Long cursoId,
                                Long aulaId, Long docenteId, String codigo, Integer capacidad,
                                String estado) {
}
