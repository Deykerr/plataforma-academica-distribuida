package servicio_evaluaciones.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_evaluaciones.dto.resultado.ValidacionPrerrequisitosRespuesta;
import servicio_evaluaciones.dto.resultado.ValidarPrerrequisitosSolicitud;
import servicio_evaluaciones.servicio.HistorialServicio;

@RestController
@RequestMapping("/api/v1/resultados")
@RequiredArgsConstructor
@Tag(name = "Resultados", description = "Validaciones académicas para otros servicios")
public class ResultadoControlador {
    private final HistorialServicio servicio;

    @PostMapping("/prerrequisitos/validacion")
    @Operation(summary = "Validar que un estudiante haya aprobado todos los cursos indicados")
    public ValidacionPrerrequisitosRespuesta validar(
            @Valid @RequestBody ValidarPrerrequisitosSolicitud solicitud) {
        return servicio.validarPrerrequisitos(solicitud);
    }
}
