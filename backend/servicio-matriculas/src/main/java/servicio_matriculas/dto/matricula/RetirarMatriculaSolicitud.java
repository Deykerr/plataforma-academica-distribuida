package servicio_matriculas.dto.matricula;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RetirarMatriculaSolicitud(@NotBlank @Size(max = 300) String motivo) {
}
