package servicio_evaluaciones.dto.integracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatriculaValidacion(Long matriculaId, boolean existe, boolean activa, String estado,
                                  Long estudianteId, Long seccionId, Long periodoId, Long cursoId) {
}
