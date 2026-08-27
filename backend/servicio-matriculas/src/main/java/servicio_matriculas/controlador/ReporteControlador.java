package servicio_matriculas.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_matriculas.dto.reporte.ResumenPeriodoRespuesta;
import servicio_matriculas.dto.seccion.SeccionRespuesta;
import servicio_matriculas.servicio.ReporteServicio;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Reportes", description = "Resumen de matriculas, ocupacion y vacantes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    @GetMapping("/periodos/{periodoId}/resumen")
    @Operation(summary = "Obtener el resumen de un periodo")
    public ResumenPeriodoRespuesta resumen(@PathVariable Long periodoId) {
        return reporteServicio.resumen(periodoId);
    }

    @GetMapping("/periodos/{periodoId}/ocupacion")
    @Operation(summary = "Obtener la ocupacion y vacantes por seccion")
    public List<SeccionRespuesta> ocupacion(@PathVariable Long periodoId) {
        return reporteServicio.ocupacion(periodoId);
    }
}
