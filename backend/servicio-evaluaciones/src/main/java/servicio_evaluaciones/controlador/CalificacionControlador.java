package servicio_evaluaciones.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import servicio_evaluaciones.dto.calificacion.*;
import servicio_evaluaciones.servicio.CalificacionServicio;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calificaciones")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Registro y correccion de notas")
public class CalificacionControlador {
    private final CalificacionServicio servicio;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Registrar una calificacion")
    public ResponseEntity<CalificacionRespuesta> crear(
            @Valid @RequestBody CrearCalificacionSolicitud solicitud) {
        CalificacionRespuesta respuesta = servicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Registrar varias calificaciones de una evaluacion")
    public ResponseEntity<List<CalificacionRespuesta>> crearLote(
            @Valid @RequestBody RegistrarCalificacionesLoteSolicitud solicitud) {
        return ResponseEntity.status(201).body(servicio.crearLote(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Listar las notas de una evaluacion")
    public List<CalificacionRespuesta> listar(@RequestParam Long evaluacionId) {
        return servicio.listarPorEvaluacion(evaluacionId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una nota con control de propietario")
    public CalificacionRespuesta obtener(@PathVariable Long id) { return servicio.obtener(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Corregir una nota mientras la evaluacion no este cerrada")
    public CalificacionRespuesta actualizar(@PathVariable Long id,
                                             @Valid @RequestBody ActualizarCalificacionSolicitud solicitud) {
        return servicio.actualizar(id, solicitud);
    }
}
