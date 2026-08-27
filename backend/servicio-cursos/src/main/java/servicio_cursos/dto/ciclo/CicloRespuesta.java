package servicio_cursos.dto.ciclo;

import servicio_cursos.dominio.Ciclo;
import servicio_cursos.dominio.EstadoRegistro;

import java.time.OffsetDateTime;

public record CicloRespuesta(Long id, Long carreraId, String carreraCodigo, Integer numero,
                            String nombre, EstadoRegistro estado, OffsetDateTime creadoEn,
                            OffsetDateTime actualizadoEn) {
    public static CicloRespuesta desde(Ciclo ciclo) {
        return new CicloRespuesta(ciclo.getId(), ciclo.getCarrera().getId(),
                ciclo.getCarrera().getCodigo(), ciclo.getNumero(), ciclo.getNombre(), ciclo.getEstado(),
                ciclo.getCreadoEn(), ciclo.getActualizadoEn());
    }
}
