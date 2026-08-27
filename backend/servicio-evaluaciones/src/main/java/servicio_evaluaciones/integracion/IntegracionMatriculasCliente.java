package servicio_evaluaciones.integracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import servicio_evaluaciones.dto.integracion.MatriculaValidacion;
import servicio_evaluaciones.dto.integracion.SeccionValidacion;
import servicio_evaluaciones.excepcion.DependenciaNoDisponibleException;
import servicio_evaluaciones.excepcion.RecursoNoEncontradoException;
import servicio_evaluaciones.seguridad.ContextoUsuario;

@Component
public class IntegracionMatriculasCliente {
    private final RestClient matriculas;
    private final ContextoUsuario contextoUsuario;

    public IntegracionMatriculasCliente(RestClient.Builder builder, ContextoUsuario contextoUsuario,
                                        @Value("${app.services.matriculas-url}") String url) {
        this.matriculas = builder.baseUrl(url).build();
        this.contextoUsuario = contextoUsuario;
    }

    public MatriculaValidacion validarMatricula(Long id) {
        return ejecutar(() -> matriculas.get().uri("/api/v1/matriculas/{id}/validacion", id)
                .header(HttpHeaders.AUTHORIZATION, contextoUsuario.bearerToken())
                .retrieve().body(MatriculaValidacion.class));
    }

    public SeccionValidacion obtenerSeccion(Long id) {
        return ejecutar(() -> matriculas.get().uri("/api/v1/secciones/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, contextoUsuario.bearerToken())
                .retrieve().body(SeccionValidacion.class));
    }

    private <T> T ejecutar(Peticion<T> peticion) {
        try {
            T respuesta = peticion.ejecutar();
            if (respuesta == null) throw new DependenciaNoDisponibleException(
                    "Servicio de Matriculas devolvio una respuesta vacia", null);
            return respuesta;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new RecursoNoEncontradoException(
                        "El recurso solicitado no existe en Servicio de Matriculas");
            }
            throw new DependenciaNoDisponibleException(
                    "No fue posible validar la informacion en Servicio de Matriculas", ex);
        } catch (ResourceAccessException ex) {
            throw new DependenciaNoDisponibleException(
                    "No fue posible validar la informacion en Servicio de Matriculas", ex);
        }
    }

    @FunctionalInterface
    private interface Peticion<T> { T ejecutar(); }
}
