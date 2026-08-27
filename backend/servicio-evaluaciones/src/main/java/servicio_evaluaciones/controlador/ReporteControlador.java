package servicio_evaluaciones.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_evaluaciones.dto.historial.HistorialAcademicoRespuesta;
import servicio_evaluaciones.dto.reporte.ResumenSeccionRespuesta;
import servicio_evaluaciones.servicio.HistorialServicio;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
@Tag(name = "Reportes", description = "Resultados y resumen por seccion")
public class ReporteControlador {
    private final HistorialServicio servicio;

    @GetMapping("/secciones/{seccionId}/resultados")
    @Operation(summary = "Listar resultados de las matriculas calificadas")
    public List<HistorialAcademicoRespuesta> resultados(@PathVariable Long seccionId) {
        return servicio.resultadosSeccion(seccionId);
    }

    @GetMapping("/secciones/{seccionId}/resumen")
    @Operation(summary = "Calcular aprobados, desaprobados y promedio general")
    public ResumenSeccionRespuesta resumen(@PathVariable Long seccionId) {
        return servicio.resumenSeccion(seccionId);
    }
}
