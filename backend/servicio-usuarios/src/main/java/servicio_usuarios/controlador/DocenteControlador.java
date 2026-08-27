package servicio_usuarios.controlador;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import servicio_usuarios.dto.comun.PaginaRespuesta;
import servicio_usuarios.dto.docente.ActualizarDocenteSolicitud;
import servicio_usuarios.dto.docente.DocenteRespuesta;
import servicio_usuarios.dto.docente.RegistroDocenteSolicitud;
import servicio_usuarios.servicio.DocenteServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
@Tag(name = "Docentes", description = "Administracion de perfiles docentes")
public class DocenteControlador {

    private final DocenteServicio docenteServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar un docente")
    public ResponseEntity<DocenteRespuesta> registrar(@Valid @RequestBody RegistroDocenteSolicitud solicitud) {
        DocenteRespuesta respuesta = docenteServicio.registrar(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Listar docentes")
    public PaginaRespuesta<DocenteRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @ParameterObject @PageableDefault(size = 20, sort = "apellidos") Pageable pageable) {
        return docenteServicio.listar(busqueda, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Obtener un docente por id")
    public DocenteRespuesta obtener(@PathVariable Long id) {
        return docenteServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un docente")
    public DocenteRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ActualizarDocenteSolicitud solicitud) {
        return docenteServicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dar de baja logicamente a un docente")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        docenteServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
