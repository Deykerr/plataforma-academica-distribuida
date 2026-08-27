package servicio_matriculas.dto.matricula;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearMatriculaSolicitud(@NotNull @Positive Long estudianteId,
                                      @NotNull @Positive Long seccionId) {
}
