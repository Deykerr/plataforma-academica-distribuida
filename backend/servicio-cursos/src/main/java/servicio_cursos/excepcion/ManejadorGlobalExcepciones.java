package servicio_cursos.excepcion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorApi> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler({ConflictoException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorApi> manejarConflicto(Exception ex, HttpServletRequest request) {
        String mensaje = ex instanceof ConflictoException ? ex.getMessage()
                : "La operacion entra en conflicto con datos existentes";
        return respuesta(HttpStatus.CONFLICT, mensaje, request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorApi> manejarRegla(ReglaNegocioException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorApi> manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operacion", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorApi> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", request, campos);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorApi> manejarSolicitudInvalida(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud no tiene un formato valido", request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorApi> manejarErrorInesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno", request, null);
    }

    private ResponseEntity<ErrorApi> respuesta(HttpStatus estado, String mensaje,
                                                HttpServletRequest request, Map<String, String> campos) {
        return ResponseEntity.status(estado).body(new ErrorApi(OffsetDateTime.now(ZoneOffset.UTC),
                estado.value(), estado.getReasonPhrase(), mensaje, request.getRequestURI(), campos));
    }
}
