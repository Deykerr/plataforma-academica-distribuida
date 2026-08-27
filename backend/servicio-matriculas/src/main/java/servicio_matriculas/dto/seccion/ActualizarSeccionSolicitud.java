package servicio_matriculas.dto.seccion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ActualizarSeccionSolicitud(
        @NotNull @Positive Long periodoId,
        @NotNull @Positive Long cursoId,
        @NotNull @Positive Long aulaId,
        @NotNull @Positive Long docenteId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,20}") String codigo,
        @NotNull @Min(1) @Max(500) Integer capacidad,
        @NotEmpty List<@Valid HorarioSolicitud> horarios
) {
}
