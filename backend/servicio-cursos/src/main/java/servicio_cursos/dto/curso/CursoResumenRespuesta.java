package servicio_cursos.dto.curso;

import servicio_cursos.dominio.Curso;

public record CursoResumenRespuesta(Long id, String codigo, String nombre, Integer creditos) {
    public static CursoResumenRespuesta desde(Curso curso) {
        return new CursoResumenRespuesta(curso.getId(), curso.getCodigo(), curso.getNombre(), curso.getCreditos());
    }
}
