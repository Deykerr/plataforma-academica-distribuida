package servicio_matriculas.dto.periodo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ActualizarPeriodoSolicitud(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{2,20}") String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        @NotNull LocalDate fechaInicioMatricula,
        @NotNull LocalDate fechaFinMatricula
) {
}
