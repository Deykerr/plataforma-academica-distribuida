package servicio_matriculas.dto.integracion;

public record AulaValidacion(Long aulaId, boolean existe, boolean disponible,
                             boolean aforoSuficiente, Integer capacidad,
                             String tipo, String estado) {
}
