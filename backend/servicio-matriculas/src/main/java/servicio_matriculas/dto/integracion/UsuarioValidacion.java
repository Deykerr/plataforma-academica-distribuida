package servicio_matriculas.dto.integracion;

import java.util.Set;

public record UsuarioValidacion(Long usuarioId, boolean existe, boolean activo,
                                String estado, Set<String> roles) {
}
