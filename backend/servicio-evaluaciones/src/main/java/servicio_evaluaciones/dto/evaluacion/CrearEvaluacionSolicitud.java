package servicio_evaluaciones.dto.evaluacion;

import jakarta.validation.constraints.*;
import servicio_evaluaciones.dominio.TipoEvaluacion;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearEvaluacionSolicitud(
        @NotNull @Positive Long seccionId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,20}") String codigo,
        @NotBlank @Size(max = 120) String nombre,
        @NotNull TipoEvaluacion tipo,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal ponderacion,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal notaMaxima,
        @NotNull LocalDate fecha) {
}
