package servicio_usuarios.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @NotBlank @Email String correo,
        @NotBlank String clave
) {
}
