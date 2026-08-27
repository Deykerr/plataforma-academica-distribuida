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
import servicio_cursos.dto.carrera.ActualizarCarreraSolicitud;
import servicio_cursos.dto.carrera.CarreraRespuesta;
import servicio_cursos.dto.carrera.CrearCarreraSolicitud;
import servicio_cursos.dto.comun.CambiarEstadoRegistroSolicitud;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.servicio.CarreraServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/carreras")
@RequiredArgsConstructor
@Tag(name = "Carreras", description = "Programas de estudio")
public class CarreraControlador {

    private final CarreraServicio carreraServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear una carrera")
    public ResponseEntity<CarreraRespuesta> crear(@Valid @RequestBody CrearCarreraSolicitud solicitud) {
        CarreraRespuesta respuesta = carreraServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar carreras")
    public PaginaRespuesta<CarreraRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) EstadoRegistro estado,
            @ParameterObject @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return carreraServicio.listar(busqueda, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una carrera")
    public CarreraRespuesta obtener(@PathVariable Long id) {
        return carreraServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una carrera")
    public CarreraRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ActualizarCarreraSolicitud solicitud) {
        return carreraServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar una carrera")
    public CarreraRespuesta cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoRegistroSolicitud solicitud) {
        return carreraServicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dar de baja logicamente una carrera")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        carreraServicio.cambiarEstado(id, EstadoRegistro.INACTIVO);
        return ResponseEntity.noContent().build();
    }
}
