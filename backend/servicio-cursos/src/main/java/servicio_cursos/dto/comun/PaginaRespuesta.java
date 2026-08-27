package servicio_cursos.dto.comun;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaRespuesta<T>(List<T> contenido, int pagina, int elementosPorPagina,
                                 long totalElementos, int totalPaginas, boolean ultima) {
    public static <T> PaginaRespuesta<T> desde(Page<T> pagina) {
        return new PaginaRespuesta<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }
}
