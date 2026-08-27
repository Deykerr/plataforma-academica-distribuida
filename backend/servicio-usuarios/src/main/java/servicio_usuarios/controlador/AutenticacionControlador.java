package servicio_usuarios.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import servicio_usuarios.dto.auth.LoginSolicitud;
import servicio_usuarios.dto.auth.TokenRespuesta;
import servicio_usuarios.servicio.AutenticacionServicio;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Obtencion de tokens JWT")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", security = {})
    @ApiResponse(responseCode = "200", description = "Credenciales validas")
    @ApiResponse(responseCode = "401", description = "Credenciales invalidas o usuario inactivo")
    public ResponseEntity<TokenRespuesta> login(@Valid @RequestBody LoginSolicitud solicitud) {
        return ResponseEntity.ok(autenticacionServicio.autenticar(solicitud));
    }
}
