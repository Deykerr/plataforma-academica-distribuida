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
import servicio_cursos.dto.comun.CambiarEstadoRegistroSolicitud;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.dto.curso.ActualizarCursoSolicitud;
import servicio_cursos.dto.curso.CrearCursoSolicitud;
import servicio_cursos.dto.curso.CursoRespuesta;
import servicio_cursos.dto.curso.ValidacionCursoRespuesta;
import servicio_cursos.servicio.CursoServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Materias, créditos y prerrequisitos")
public class CursoControlador {

    private final CursoServicio cursoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un curso")
    public ResponseEntity<CursoRespuesta> crear(@Valid @RequestBody CrearCursoSolicitud solicitud) {
        CursoRespuesta respuesta = cursoServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar y buscar cursos")
    public PaginaRespuesta<CursoRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long carreraId,
            @RequestParam(required = false) Long cicloId,
            @RequestParam(required = false) EstadoRegistro estado,
            @ParameterObject @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return cursoServicio.listar(busqueda, carreraId, cicloId, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un curso")
    public CursoRespuesta obtener(@PathVariable Long id) {
        return cursoServicio.obtener(id);
    }

    @GetMapping("/{id}/validacion")
    @Operation(summary = "Validar un curso para otros microservicios")
    public ValidacionCursoRespuesta validar(@PathVariable Long id) {
        return cursoServicio.validar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un curso y sus prerrequisitos")
    public CursoRespuesta actualizar(@PathVariable Long id,
                                     @Valid @RequestBody ActualizarCursoSolicitud solicitud) {
        return cursoServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar un curso")
    public CursoRespuesta cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRegistroSolicitud solicitud) {
        return cursoServicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dar de baja logicamente un curso")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        cursoServicio.cambiarEstado(id, EstadoRegistro.INACTIVO);
        return ResponseEntity.noContent().build();
    }
}
