package servicio_matriculas.dto.seccion;

import servicio_matriculas.dominio.DiaSemana;

import java.time.LocalTime;

public record HorarioRespuesta(Long id, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin) {
}
