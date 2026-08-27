package servicio_cursos.dto.curso;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CrearCursoSolicitud(
        @NotNull @Positive Long carreraId,
        @NotNull @Positive Long cicloId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{2,20}") String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @Min(1) @Max(10) Integer creditos,
        @NotNull @Min(0) @Max(20) Integer horasTeoria,
        @NotNull @Min(0) @Max(20) Integer horasPractica,
        Set<@Positive Long> prerequisitoIds
) {
}
