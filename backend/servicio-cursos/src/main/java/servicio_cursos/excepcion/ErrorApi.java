package servicio_cursos.excepcion;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorApi(OffsetDateTime fechaHora, int estado, String error, String mensaje,
                       String ruta, Map<String, String> campos) {
}
