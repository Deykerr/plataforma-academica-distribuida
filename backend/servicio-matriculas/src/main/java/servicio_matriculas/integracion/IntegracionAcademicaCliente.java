package servicio_matriculas.integracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import servicio_matriculas.dto.integracion.AulaValidacion;
import servicio_matriculas.dto.integracion.CursoValidacion;
import servicio_matriculas.dto.integracion.UsuarioValidacion;
import servicio_matriculas.excepcion.DependenciaNoDisponibleException;
import servicio_matriculas.seguridad.ContextoUsuario;

@Component
public class IntegracionAcademicaCliente {

    private final RestClient usuarios;
    private final RestClient cursos;
    private final ContextoUsuario contextoUsuario;

    public IntegracionAcademicaCliente(RestClient.Builder builder, ContextoUsuario contextoUsuario,
                                       @Value("${app.services.usuarios-url}") String usuariosUrl,
                                       @Value("${app.services.cursos-url}") String cursosUrl) {
        this.usuarios = builder.clone().baseUrl(usuariosUrl).build();
        this.cursos = builder.clone().baseUrl(cursosUrl).build();
        this.contextoUsuario = contextoUsuario;
    }

    public UsuarioValidacion validarUsuario(Long usuarioId, String rol) {
        return ejecutar("Servicio de Usuarios", () -> usuarios.get()
                .uri(uri -> uri.path("/api/v1/usuarios/{id}/validacion")
                        .queryParam("rol", rol).build(usuarioId))
                .header(HttpHeaders.AUTHORIZATION, contextoUsuario.bearerToken())
                .retrieve().body(UsuarioValidacion.class));
    }

    public CursoValidacion validarCurso(Long cursoId) {
        return ejecutar("Servicio de Cursos", () -> cursos.get()
                .uri("/api/v1/cursos/{id}/validacion", cursoId)
                .header(HttpHeaders.AUTHORIZATION, contextoUsuario.bearerToken())
                .retrieve().body(CursoValidacion.class));
    }

    public AulaValidacion validarAula(Long aulaId, Integer aforo) {
        return ejecutar("Servicio de Cursos", () -> cursos.get()
                .uri(uri -> uri.path("/api/v1/aulas/{id}/validacion")
                        .queryParam("aforoRequerido", aforo).build(aulaId))
                .header(HttpHeaders.AUTHORIZATION, contextoUsuario.bearerToken())
                .retrieve().body(AulaValidacion.class));
    }

    private <T> T ejecutar(String servicio, Peticion<T> peticion) {
        try {
            T respuesta = peticion.ejecutar();
            if (respuesta == null) {
                throw new DependenciaNoDisponibleException(servicio + " devolvio una respuesta vacia", null);
            }
            return respuesta;
        } catch (RestClientResponseException | ResourceAccessException ex) {
            throw new DependenciaNoDisponibleException(
                    "No fue posible validar la informacion en " + servicio, ex);
        }
    }

    @FunctionalInterface
    private interface Peticion<T> {
        T ejecutar();
    }
}
