package servicio_cursos.dto.carrera;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearCarreraSolicitud(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{2,20}") String codigo,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @Min(1) @Max(15) Integer duracionCiclos
) {
}
