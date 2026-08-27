package servicio_cursos.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_cursos.dto.catalogo.CatalogoCarreraRespuesta;
import servicio_cursos.servicio.CatalogoServicio;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catalogo", description = "Oferta académica activa y consolidada")
public class CatalogoControlador {

    private final CatalogoServicio catalogoServicio;

    @GetMapping
    @Operation(summary = "Consultar carreras, ciclos y cursos activos")
    public List<CatalogoCarreraRespuesta> obtenerCatalogo() {
        return catalogoServicio.obtenerCatalogoActivo();
    }
}
