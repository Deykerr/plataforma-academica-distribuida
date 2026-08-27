package servicio_cursos.dto.catalogo;

import servicio_cursos.dominio.Curso;
import servicio_cursos.dto.curso.CursoResumenRespuesta;

import java.util.Comparator;
import java.util.List;

public record CatalogoCursoRespuesta(Long id, String codigo, String nombre, Integer creditos,
                                    Integer horasTeoria, Integer horasPractica,
                                    List<CursoResumenRespuesta> prerequisitos) {
    public static CatalogoCursoRespuesta desde(Curso curso) {
        return new CatalogoCursoRespuesta(curso.getId(), curso.getCodigo(), curso.getNombre(),
                curso.getCreditos(), curso.getHorasTeoria(), curso.getHorasPractica(),
                curso.getPrerequisitos().stream().sorted(Comparator.comparing(Curso::getCodigo))
                        .map(CursoResumenRespuesta::desde).toList());
    }
}
