package servicio_matriculas.dto.seccion;

import jakarta.validation.constraints.NotNull;
import servicio_matriculas.dominio.DiaSemana;

import java.time.LocalTime;

public record HorarioSolicitud(@NotNull DiaSemana diaSemana, @NotNull LocalTime horaInicio,
                               @NotNull LocalTime horaFin) {
}
