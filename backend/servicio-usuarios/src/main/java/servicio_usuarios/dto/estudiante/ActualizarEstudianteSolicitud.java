package servicio_usuarios.dto.estudiante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ActualizarEstudianteSolicitud(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{6,20}") String documentoIdentidad,
        @NotNull @Past LocalDate fechaNacimiento,
        @Pattern(regexp = "[0-9+() -]{7,20}") String telefono,
        @Size(max = 200) String direccion,
        @Positive Long carreraId
) {
}
