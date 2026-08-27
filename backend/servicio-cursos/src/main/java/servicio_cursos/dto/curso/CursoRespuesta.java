package servicio_cursos.dto.curso;

import servicio_cursos.dominio.Curso;
import servicio_cursos.dominio.EstadoRegistro;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record CursoRespuesta(
        Long id, Long carreraId, String carreraCodigo, Long cicloId, Integer cicloNumero,
        String codigo, String nombre, String descripcion, Integer creditos,
        Integer horasTeoria, Integer horasPractica, EstadoRegistro estado,
        List<CursoResumenRespuesta> prerequisitos, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn
) {
    public static CursoRespuesta desde(Curso curso) {
        List<CursoResumenRespuesta> prerequisitos = curso.getPrerequisitos().stream()
                .sorted(Comparator.comparing(Curso::getCodigo))
                .map(CursoResumenRespuesta::desde)
                .toList();
        return new CursoRespuesta(curso.getId(), curso.getCarrera().getId(), curso.getCarrera().getCodigo(),
                curso.getCiclo().getId(), curso.getCiclo().getNumero(), curso.getCodigo(), curso.getNombre(),
                curso.getDescripcion(), curso.getCreditos(), curso.getHorasTeoria(), curso.getHorasPractica(),
                curso.getEstado(), prerequisitos, curso.getCreadoEn(), curso.getActualizadoEn());
    }
}
