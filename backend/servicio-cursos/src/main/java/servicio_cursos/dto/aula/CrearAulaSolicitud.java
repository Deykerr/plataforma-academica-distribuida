package servicio_cursos.dto.aula;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import servicio_cursos.dominio.TipoAula;

public record CrearAulaSolicitud(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{2,20}") String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull TipoAula tipo,
        @NotNull @Min(1) @Max(500) Integer capacidad,
        @NotBlank @Size(max = 200) String ubicacion
) {
}
