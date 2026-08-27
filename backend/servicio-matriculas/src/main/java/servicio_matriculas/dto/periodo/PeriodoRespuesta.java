package servicio_matriculas.dto.periodo;

import servicio_matriculas.dominio.EstadoPeriodo;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PeriodoRespuesta(Long id, String codigo, String nombre, LocalDate fechaInicio,
                               LocalDate fechaFin, LocalDate fechaInicioMatricula,
                               LocalDate fechaFinMatricula, EstadoPeriodo estado,
                               boolean matriculaVigente, OffsetDateTime creadoEn,
                               OffsetDateTime actualizadoEn) {
}
