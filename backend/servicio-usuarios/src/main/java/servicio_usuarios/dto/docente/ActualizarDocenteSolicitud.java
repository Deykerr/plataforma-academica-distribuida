package servicio_usuarios.dto.docente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarDocenteSolicitud(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{6,20}") String documentoIdentidad,
        @NotBlank @Size(max = 120) String especialidad,
        @Pattern(regexp = "[0-9+() -]{7,20}") String telefono
) {
}
