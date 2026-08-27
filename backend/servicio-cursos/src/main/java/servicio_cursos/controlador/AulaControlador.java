package servicio_cursos.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import servicio_cursos.dominio.EstadoAula;
import servicio_cursos.dominio.TipoAula;
import servicio_cursos.dto.aula.ActualizarAulaSolicitud;
import servicio_cursos.dto.aula.AulaRespuesta;
import servicio_cursos.dto.aula.CambiarEstadoAulaSolicitud;
import servicio_cursos.dto.aula.CrearAulaSolicitud;
import servicio_cursos.dto.aula.ValidacionAulaRespuesta;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.servicio.AulaServicio;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/v1/aulas")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Espacios físicos, laboratorios y aforo")
public class AulaControlador {

    private final AulaServicio aulaServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un aula o laboratorio")
    public ResponseEntity<AulaRespuesta> crear(@Valid @RequestBody CrearAulaSolicitud solicitud) {
        AulaRespuesta respuesta = aulaServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar aulas y laboratorios")
    public PaginaRespuesta<AulaRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) TipoAula tipo,
            @RequestParam(required = false) EstadoAula estado,
            @ParameterObject @PageableDefault(size = 20, sort = "codigo") Pageable pageable) {
        return aulaServicio.listar(busqueda, tipo, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un aula")
    public AulaRespuesta obtener(@PathVariable Long id) {
        return aulaServicio.obtener(id);
    }

    @GetMapping("/{id}/validacion")
    @Operation(summary = "Validar disponibilidad y aforo para Matrículas")
    public ValidacionAulaRespuesta validar(@PathVariable Long id,
                                           @RequestParam(required = false) @Positive Integer aforoRequerido) {
        return aulaServicio.validar(id, aforoRequerido);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un aula")
    public AulaRespuesta actualizar(@PathVariable Long id,
                                    @Valid @RequestBody ActualizarAulaSolicitud solicitud) {
        return aulaServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar disponibilidad de un aula")
    public AulaRespuesta cambiarEstado(@PathVariable Long id,
                                       @Valid @RequestBody CambiarEstadoAulaSolicitud solicitud) {
        return aulaServicio.cambiarEstado(id, solicitud.estado());
    }
}
