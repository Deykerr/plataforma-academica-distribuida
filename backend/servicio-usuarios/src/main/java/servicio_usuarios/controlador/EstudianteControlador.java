package servicio_usuarios.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import servicio_usuarios.dto.estudiante.ActualizarEstudianteSolicitud;
import servicio_usuarios.dto.estudiante.EstudianteRespuesta;
import servicio_usuarios.dto.estudiante.RegistroEstudianteSolicitud;
import servicio_usuarios.servicio.EstudianteServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Registro y administracion de perfiles estudiantiles")
public class EstudianteControlador {

    private final EstudianteServicio estudianteServicio;

    @PostMapping
    @Operation(summary = "Registrar un estudiante", description = "Alta publica limitada al rol ESTUDIANTE", security = {})
    @ApiResponse(responseCode = "201", description = "Estudiante registrado")
    @ApiResponse(responseCode = "409", description = "Correo, codigo o documento duplicado")
    public ResponseEntity<EstudianteRespuesta> registrar(
            @Valid @RequestBody RegistroEstudianteSolicitud solicitud) {
        EstudianteRespuesta respuesta = estudianteServicio.registrar(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Listar estudiantes", description = "Admite busqueda y paginacion")
    public PaginaRespuesta<EstudianteRespuesta> listar(
            @RequestParam(required = false) String busqueda,
            @ParameterObject @PageableDefault(size = 20, sort = "apellidos") Pageable pageable) {
        return estudianteServicio.listar(busqueda, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    @Operation(summary = "Obtener un estudiante por id")
    public EstudianteRespuesta obtener(@Parameter(description = "Id interno del perfil") @PathVariable Long id) {
        return estudianteServicio.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un estudiante como administrador")
    public EstudianteRespuesta actualizar(@PathVariable Long id,
                                           @Valid @RequestBody ActualizarEstudianteSolicitud solicitud) {
        return estudianteServicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dar de baja logicamente a un estudiante")
    @ApiResponse(responseCode = "204", description = "Usuario del estudiante desactivado")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        estudianteServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Consultar el perfil del estudiante autenticado")
    public EstudianteRespuesta miPerfil(Authentication authentication) {
        return estudianteServicio.obtenerPropio(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Actualizar el perfil del estudiante autenticado")
    public EstudianteRespuesta actualizarMiPerfil(Authentication authentication,
                                                   @Valid @RequestBody ActualizarEstudianteSolicitud solicitud) {
        return estudianteServicio.actualizarPropio(authentication.getName(), solicitud);
    }
}
