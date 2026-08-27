package servicio_cursos.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import servicio_cursos.dominio.EstadoRegistro;
import servicio_cursos.dto.ciclo.ActualizarCicloSolicitud;
import servicio_cursos.dto.ciclo.CicloRespuesta;
import servicio_cursos.dto.ciclo.CrearCicloSolicitud;
import servicio_cursos.dto.comun.CambiarEstadoRegistroSolicitud;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.servicio.CicloServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ciclos")
@RequiredArgsConstructor
@Tag(name = "Ciclos", description = "Niveles académicos de cada carrera")
public class CicloControlador {

    private final CicloServicio cicloServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un ciclo")
    public ResponseEntity<CicloRespuesta> crear(@Valid @RequestBody CrearCicloSolicitud solicitud) {
        CicloRespuesta respuesta = cicloServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar ciclos")
    public PaginaRespuesta<CicloRespuesta> listar(
            @RequestParam(required = false) Long carreraId,
            @RequestParam(required = false) EstadoRegistro estado,
            @ParameterObject @PageableDefault(size = 20, sort = "numero") Pageable pageable) {
        return cicloServicio.listar(carreraId, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un ciclo")
    public CicloRespuesta obtener(@PathVariable Long id) {
        return cicloServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un ciclo")
    public CicloRespuesta actualizar(@PathVariable Long id,
                                     @Valid @RequestBody ActualizarCicloSolicitud solicitud) {
        return cicloServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar un ciclo")
    public CicloRespuesta cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRegistroSolicitud solicitud) {
        return cicloServicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dar de baja logicamente un ciclo")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        cicloServicio.cambiarEstado(id, EstadoRegistro.INACTIVO);
        return ResponseEntity.noContent().build();
    }
}
