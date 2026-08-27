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
import servicio_matriculas.dominio.EstadoSeccion;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.seccion.ActualizarSeccionSolicitud;
import servicio_matriculas.dto.seccion.CambiarEstadoSeccionSolicitud;
import servicio_matriculas.dto.seccion.CrearSeccionSolicitud;
import servicio_matriculas.dto.seccion.SeccionRespuesta;
import servicio_matriculas.servicio.SeccionServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/secciones")
@RequiredArgsConstructor
@Tag(name = "Secciones", description = "Oferta real de cursos, horarios, docentes, aulas y vacantes")
public class SeccionControlador {

    private final SeccionServicio seccionServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear una seccion validando curso, aula y docente")
    public ResponseEntity<SeccionRespuesta> crear(@Valid @RequestBody CrearSeccionSolicitud solicitud) {
        SeccionRespuesta respuesta = seccionServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @Operation(summary = "Listar secciones y sus vacantes")
    public PaginaRespuesta<SeccionRespuesta> listar(
            @RequestParam(required = false) Long periodoId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Long docenteId,
            @RequestParam(required = false) EstadoSeccion estado,
            @ParameterObject @PageableDefault(size = 20, sort = "codigo") Pageable pageable) {
        return seccionServicio.listar(periodoId, cursoId, docenteId, estado, pageable);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Listar las secciones asignadas al docente autenticado")
    public PaginaRespuesta<SeccionRespuesta> mias(
            @RequestParam(required = false) Long periodoId,
            @ParameterObject @PageableDefault(size = 20, sort = "codigo") Pageable pageable) {
        return seccionServicio.misSecciones(periodoId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una seccion")
    public SeccionRespuesta obtener(@PathVariable Long id) {
        return seccionServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una seccion sin matriculas activas")
    public SeccionRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ActualizarSeccionSolicitud solicitud) {
        return seccionServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Abrir, cerrar, iniciar, finalizar o cancelar una seccion")
    public SeccionRespuesta cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoSeccionSolicitud solicitud) {
        return seccionServicio.cambiarEstado(id, solicitud.estado());
    }
}
