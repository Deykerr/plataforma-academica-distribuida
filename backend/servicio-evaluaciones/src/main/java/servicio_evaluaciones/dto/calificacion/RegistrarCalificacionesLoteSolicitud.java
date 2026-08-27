package servicio_evaluaciones.dto.calificacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record RegistrarCalificacionesLoteSolicitud(
        @NotNull @Positive Long evaluacionId,
        @NotEmpty List<@Valid ItemCalificacionSolicitud> calificaciones) {
}
