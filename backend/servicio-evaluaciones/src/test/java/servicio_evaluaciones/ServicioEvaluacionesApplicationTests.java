package servicio_evaluaciones;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import servicio_evaluaciones.dto.integracion.MatriculaValidacion;
import servicio_evaluaciones.dto.integracion.SeccionValidacion;
import servicio_evaluaciones.integracion.IntegracionMatriculasCliente;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicioEvaluacionesApplicationTests {
    private static final String SECRETO = "clave-super-segura-para-pruebas-automatizadas-123456";
    @Value("${local.server.port}") private int puerto;
    @MockitoBean private IntegracionMatriculasCliente integracion;
    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void flujoCompletoDeEvaluacionesNotasPromediosYSeguridad() throws Exception {
        prepararIntegracion();
        String admin = token(1L, "ADMINISTRADOR");
        String docente = token(200L, "DOCENTE");
        String otroDocente = token(201L, "DOCENTE");
        String estudiante = token(100L, "ESTUDIANTE");
        String otroEstudiante = token(101L, "ESTUDIANTE");

        assertEquals(401, enviar("GET", "/api/v1/evaluaciones", null, null).statusCode());
        assertEquals(403, crearEvaluacion("P1", "Practica 1", "PRACTICA", "30.00", otroDocente).statusCode());
        assertEquals(403, crearEvaluacion("P1", "Practica 1", "PRACTICA", "30.00", estudiante).statusCode());

        HttpResponse<String> creada1 = crearEvaluacion("P1", "Practica 1", "PRACTICA", "30.00", docente);
        assertEquals(201, creada1.statusCode());
        long evaluacion1 = id(creada1.body());
        long evaluacion2 = id(crearEvaluacion("EP", "Examen parcial", "PARCIAL", "30.00", docente).body());
        long evaluacion3 = id(crearEvaluacion("EF", "Examen final", "FINAL", "40.00", admin).body());
        assertEquals(400, crearEvaluacion("EX", "Exceso", "OTRO", "1.00", admin).statusCode());
        assertEquals(409, crearEvaluacion("P1", "Duplicada", "TAREA", "10.00", admin).statusCode());

        HttpResponse<String> nota1 = enviar("POST", "/api/v1/calificaciones", """
                {"evaluacionId":%d,"matriculaId":1000,"valor":16.00,"observacion":"Buen trabajo"}
                """.formatted(evaluacion1), docente);
        assertEquals(201, nota1.statusCode());
        long calificacionId = id(nota1.body());
        assertEquals(409, enviar("POST", "/api/v1/calificaciones", """
                {"evaluacionId":%d,"matriculaId":1000,"valor":17.00}
                """.formatted(evaluacion1), docente).statusCode());
        assertEquals(400, enviar("POST", "/api/v1/calificaciones", """
                {"evaluacionId":%d,"matriculaId":1001,"valor":21.00}
                """.formatted(evaluacion1), docente).statusCode());
        assertEquals(201, enviar("POST", "/api/v1/calificaciones/lote", """
                {"evaluacionId":%d,"calificaciones":[
                  {"matriculaId":1001,"valor":8.00,"observacion":"Debe mejorar"}
                ]}
                """.formatted(evaluacion1), docente).statusCode());
        assertEquals(201, enviar("POST", "/api/v1/calificaciones", """
                {"evaluacionId":%d,"matriculaId":1000,"valor":12.00}
                """.formatted(evaluacion2), docente).statusCode());
        assertEquals(201, enviar("POST", "/api/v1/calificaciones", """
                {"evaluacionId":%d,"matriculaId":1000,"valor":10.00}
                """.formatted(evaluacion3), admin).statusCode());
        assertEquals(400, enviar("PUT", "/api/v1/evaluaciones/" + evaluacion1, """
                {"codigo":"P1","nombre":"Practica 1","tipo":"PRACTICA",
                 "ponderacion":30.00,"notaMaxima":10.00,"fecha":"%s"}
                """.formatted(LocalDate.now()), docente).statusCode());

        HttpResponse<String> borrador = enviar("GET", "/api/v1/historial/matriculas/1000", null, estudiante);
        assertEquals(200, borrador.statusCode());
        assertTrue(borrador.body().contains("\"ponderacionConfigurada\":0"));

        publicar(evaluacion1, docente);
        publicar(evaluacion2, docente);
        publicar(evaluacion3, admin);
        HttpResponse<String> historial = enviar("GET", "/api/v1/historial/matriculas/1000", null, estudiante);
        assertEquals(200, historial.statusCode());
        assertTrue(historial.body().contains("\"promedioAcumulado\":12.40"));
        assertTrue(historial.body().contains("\"estadoFinal\":\"APROBADO\""));
        assertEquals(403, enviar("GET", "/api/v1/historial/matriculas/1000", null, otroEstudiante).statusCode());
        assertEquals(403, enviar("GET", "/api/v1/historial/matriculas/1000", null, docente).statusCode());

        assertEquals(200, enviar("GET", "/api/v1/reportes/secciones/10/resultados", null, docente).statusCode());
        HttpResponse<String> resumen = enviar("GET", "/api/v1/reportes/secciones/10/resumen", null, docente);
        assertEquals(200, resumen.statusCode());
        assertTrue(resumen.body().contains("\"aprobados\":1"));
        assertEquals(403, enviar("GET", "/api/v1/reportes/secciones/10/resumen", null, otroDocente).statusCode());

        assertEquals(200, enviar("PUT", "/api/v1/calificaciones/" + calificacionId,
                "{\"valor\":18.00,\"observacion\":\"Correccion\"}", docente).statusCode());
        assertEquals(200, enviar("GET", "/api/v1/calificaciones/" + calificacionId, null, estudiante).statusCode());
        assertEquals(403, enviar("GET", "/api/v1/calificaciones/" + calificacionId, null, otroEstudiante).statusCode());
        assertEquals(200, enviar("PATCH", "/api/v1/evaluaciones/" + evaluacion1 + "/estado",
                "{\"estado\":\"CERRADA\"}", docente).statusCode());
        assertEquals(400, enviar("PUT", "/api/v1/calificaciones/" + calificacionId,
                "{\"valor\":19.00}", docente).statusCode());

        HttpResponse<String> openApi = enviar("GET", "/v3/api-docs", null, null);
        assertEquals(200, openApi.statusCode());
        assertTrue(openApi.body().contains("API del Servicio de Evaluaciones"));
    }

    private HttpResponse<String> crearEvaluacion(String codigo, String nombre, String tipo,
                                                 String ponderacion, String jwt) throws Exception {
        return enviar("POST", "/api/v1/evaluaciones", """
                {"seccionId":10,"codigo":"%s","nombre":"%s","tipo":"%s",
                 "ponderacion":%s,"notaMaxima":20.00,"fecha":"%s"}
                """.formatted(codigo, nombre, tipo, ponderacion, LocalDate.now()), jwt);
    }

    private void publicar(long id, String jwt) throws Exception {
        assertEquals(200, enviar("PATCH", "/api/v1/evaluaciones/" + id + "/estado",
                "{\"estado\":\"PUBLICADA\"}", jwt).statusCode());
    }

    private void prepararIntegracion() {
        when(integracion.obtenerSeccion(10L)).thenReturn(new SeccionValidacion(
                10L, 20L, "2026-II", 30L, 40L, 200L, "A", 30, "EN_CURSO"));
        when(integracion.validarMatricula(anyLong())).thenAnswer(invocacion -> {
            Long id = invocacion.getArgument(0);
            if (id == 1000L) return new MatriculaValidacion(id, true, true,
                    "ACTIVA", 100L, 10L, 20L, 30L);
            if (id == 1001L) return new MatriculaValidacion(id, true, true,
                    "ACTIVA", 101L, 10L, 20L, 30L);
            return new MatriculaValidacion(id, false, false, null, null, null, null, null);
        });
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, String jwt) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + puerto + ruta));
        if (jwt != null) builder.header("Authorization", "Bearer " + jwt);
        if (cuerpo != null) builder.header("Content-Type", "application/json");
        builder.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));
        return cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private long id(String cuerpo) throws Exception {
        JsonNode nodo = objectMapper.readTree(cuerpo);
        return nodo.get("id").asLong();
    }

    private String token(Long usuarioId, String rol) {
        SecretKey key = new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("servicio-usuarios")
                .subject(usuarioId.toString()).issuedAt(ahora).expiresAt(ahora.plusSeconds(3600))
                .claim("usuarioId", usuarioId).claim("roles", List.of(rol)).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
