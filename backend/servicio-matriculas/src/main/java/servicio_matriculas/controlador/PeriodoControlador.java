package servicio_matriculas.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import servicio_matriculas.dominio.EstadoPeriodo;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.periodo.ActualizarPeriodoSolicitud;
import servicio_matriculas.dto.periodo.CambiarEstadoPeriodoSolicitud;
import servicio_matriculas.dto.periodo.CrearPeriodoSolicitud;
import servicio_matriculas.dto.periodo.PeriodoRespuesta;
import servicio_matriculas.servicio.PeriodoServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/periodos")
@RequiredArgsConstructor
@Tag(name = "Periodos", description = "Semestres y ventanas del proceso de matricula")
public class PeriodoControlador {

    private final PeriodoServicio periodoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un periodo academico")
    public ResponseEntity<PeriodoRespuesta> crear(@Valid @RequestBody CrearPeriodoSolicitud solicitud) {
        PeriodoRespuesta respuesta = periodoServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar periodos")
    public PaginaRespuesta<PeriodoRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) EstadoPeriodo estado,
            @ParameterObject @PageableDefault(size = 20, sort = "fechaInicio") Pageable pageable) {
        return periodoServicio.listar(busqueda, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un periodo")
    public PeriodoRespuesta obtener(@PathVariable Long id) {
        return periodoServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un periodo planificado")
    public PeriodoRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ActualizarPeriodoSolicitud solicitud) {
        return periodoServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar el estado de un periodo")
    public PeriodoRespuesta cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoPeriodoSolicitud solicitud) {
        return periodoServicio.cambiarEstado(id, solicitud.estado());
    }
}
