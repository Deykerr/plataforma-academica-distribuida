package servicio_evaluaciones.dto.resultado;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record ValidarPrerrequisitosSolicitud(
        @NotNull @Positive Long estudianteId,
        @NotEmpty Set<@Positive Long> cursoIds) {
}
