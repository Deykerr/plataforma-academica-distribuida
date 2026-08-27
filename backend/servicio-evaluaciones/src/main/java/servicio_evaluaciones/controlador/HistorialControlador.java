package servicio_evaluaciones.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_evaluaciones.dto.historial.HistorialAcademicoRespuesta;
import servicio_evaluaciones.servicio.HistorialServicio;

@RestController
@RequestMapping("/api/v1/historial")
@RequiredArgsConstructor
@Tag(name = "Historial academico", description = "Notas publicadas y resultado consolidado")
public class HistorialControlador {
    private final HistorialServicio servicio;

    @GetMapping("/matriculas/{matriculaId}")
    @Operation(summary = "Consultar historial; solo titular o administrador")
    public HistorialAcademicoRespuesta historial(@PathVariable Long matriculaId) {
        return servicio.historial(matriculaId);
    }
}
