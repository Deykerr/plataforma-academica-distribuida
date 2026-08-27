package servicio_evaluaciones.dto.calificacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CrearCalificacionSolicitud(
        @NotNull @Positive Long evaluacionId,
        @NotNull @Positive Long matriculaId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal valor,
        @Size(max = 500) String observacion) {
}
