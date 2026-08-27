package servicio_evaluaciones.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import servicio_evaluaciones.dominio.EstadoEvaluacion;
import servicio_evaluaciones.dominio.TipoEvaluacion;
import servicio_evaluaciones.dto.comun.PaginaRespuesta;
import servicio_evaluaciones.dto.evaluacion.*;
import servicio_evaluaciones.servicio.EvaluacionServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Tag(name = "Evaluaciones", description = "Componentes, ponderaciones y publicacion")
public class EvaluacionControlador {
    private final EvaluacionServicio servicio;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Crear un componente de evaluacion para una seccion")
    public ResponseEntity<EvaluacionRespuesta> crear(@Valid @RequestBody CrearEvaluacionSolicitud solicitud) {
        EvaluacionRespuesta respuesta = servicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Listar evaluaciones con filtros")
    public PaginaRespuesta<EvaluacionRespuesta> listar(
            @RequestParam(required = false) Long seccionId,
            @RequestParam(required = false) Long periodoId,
            @RequestParam(required = false) EstadoEvaluacion estado,
            @RequestParam(required = false) TipoEvaluacion tipo,
            @ParameterObject @PageableDefault(size = 20, sort = "fecha") Pageable pageable) {
        return servicio.listar(seccionId, periodoId, estado, tipo, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public EvaluacionRespuesta obtener(@PathVariable Long id) { return servicio.obtener(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public EvaluacionRespuesta actualizar(@PathVariable Long id,
                                           @Valid @RequestBody ActualizarEvaluacionSolicitud solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public EvaluacionRespuesta cambiarEstado(@PathVariable Long id,
                                              @Valid @RequestBody CambiarEstadoEvaluacionSolicitud solicitud) {
        return servicio.cambiarEstado(id, solicitud.estado());
    }
}
