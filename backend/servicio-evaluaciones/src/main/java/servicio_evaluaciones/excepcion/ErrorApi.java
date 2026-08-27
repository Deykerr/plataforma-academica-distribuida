package servicio_evaluaciones.excepcion;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorApi(OffsetDateTime timestamp, int status, String error, String mensaje,
                       String ruta, Map<String, String> campos) {
}
