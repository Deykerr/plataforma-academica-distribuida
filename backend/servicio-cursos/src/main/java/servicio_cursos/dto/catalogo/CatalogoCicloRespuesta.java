package servicio_cursos.dto.catalogo;

import java.util.List;

public record CatalogoCicloRespuesta(Long id, Integer numero, String nombre,
                                    List<CatalogoCursoRespuesta> cursos) {
}
