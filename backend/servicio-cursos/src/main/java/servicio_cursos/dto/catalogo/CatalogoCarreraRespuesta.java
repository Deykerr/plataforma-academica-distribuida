package servicio_cursos.dto.catalogo;

import java.util.List;

public record CatalogoCarreraRespuesta(Long id, String codigo, String nombre,
                                      Integer duracionCiclos, List<CatalogoCicloRespuesta> ciclos) {
}
