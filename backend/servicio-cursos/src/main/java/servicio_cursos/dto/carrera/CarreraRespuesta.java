package servicio_cursos.dto.carrera;

import servicio_cursos.dominio.Carrera;
import servicio_cursos.dominio.EstadoRegistro;

import java.time.OffsetDateTime;

public record CarreraRespuesta(Long id, String codigo, String nombre, String descripcion,
                               Integer duracionCiclos, EstadoRegistro estado,
                               OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
    public static CarreraRespuesta desde(Carrera carrera) {
        return new CarreraRespuesta(carrera.getId(), carrera.getCodigo(), carrera.getNombre(),
                carrera.getDescripcion(), carrera.getDuracionCiclos(), carrera.getEstado(),
                carrera.getCreadoEn(), carrera.getActualizadoEn());
    }
}
