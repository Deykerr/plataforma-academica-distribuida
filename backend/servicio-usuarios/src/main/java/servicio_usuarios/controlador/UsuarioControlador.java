package servicio_usuarios.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dto.usuario.ActualizarRolesSolicitud;
import servicio_usuarios.dto.usuario.CambiarEstadoSolicitud;
import servicio_usuarios.dto.usuario.UsuarioRespuesta;
import servicio_usuarios.dto.usuario.ValidacionUsuarioRespuesta;
import servicio_usuarios.servicio.UsuarioServicio;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Credenciales, roles, estado y validacion entre servicios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener los datos de acceso de un usuario")
    public UsuarioRespuesta obtener(@PathVariable Long id) {
        return usuarioServicio.obtener(id);
    }

    @GetMapping("/{id}/validacion")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Validar existencia, estado y rol",
            description = "Endpoint destinado a los futuros servicios de matriculas y evaluaciones")
    public ValidacionUsuarioRespuesta validar(@PathVariable Long id,
                                               @RequestParam(required = false) RolUsuario rol) {
        return usuarioServicio.validar(id, rol);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar un usuario")
    public UsuarioRespuesta cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoSolicitud solicitud) {
        return usuarioServicio.cambiarEstado(id, solicitud.estado());
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar los roles de un usuario")
    public UsuarioRespuesta cambiarRoles(@PathVariable Long id,
                                          @Valid @RequestBody ActualizarRolesSolicitud solicitud) {
        return usuarioServicio.cambiarRoles(id, solicitud.roles());
    }
}
