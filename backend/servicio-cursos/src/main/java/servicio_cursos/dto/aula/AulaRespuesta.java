package servicio_cursos.dto.aula;

import servicio_cursos.dominio.Aula;
import servicio_cursos.dominio.EstadoAula;
import servicio_cursos.dominio.TipoAula;

import java.time.OffsetDateTime;

public record AulaRespuesta(Long id, String codigo, String nombre, TipoAula tipo,
                           Integer capacidad, String ubicacion, EstadoAula estado,
                           OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
    public static AulaRespuesta desde(Aula aula) {
        return new AulaRespuesta(aula.getId(), aula.getCodigo(), aula.getNombre(), aula.getTipo(),
                aula.getCapacidad(), aula.getUbicacion(), aula.getEstado(),
                aula.getCreadoEn(), aula.getActualizadoEn());
    }
}
