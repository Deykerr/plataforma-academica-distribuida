package servicio_cursos;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicioCursosApplicationTests {

    private static final String SECRETO = "clave-super-segura-para-pruebas-automatizadas-123456";

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void flujoCompletoDelCatalogoYSeguridad() throws Exception {
        HttpResponse<String> sinToken = enviar("GET", "/api/v1/carreras", null, null);
        assertEquals(401, sinToken.statusCode());

        String tokenAdmin = generarToken("admin@academica.test", List.of("ADMINISTRADOR"));
        String tokenEstudiante = generarToken("alumna@academica.test", List.of("ESTUDIANTE"));

        String carreraJson = """
                {
                  "codigo": "ISI",
                  "nombre": "Ingenieria de Sistemas de Informacion",
                  "descripcion": "Programa orientado al desarrollo y gestion de sistemas",
                  "duracionCiclos": 10
                }
                """;
        HttpResponse<String> carrera = enviar("POST", "/api/v1/carreras", carreraJson, tokenAdmin);
        assertEquals(201, carrera.statusCode());
        long carreraId = id(carrera.body());

        HttpResponse<String> duplicada = enviar("POST", "/api/v1/carreras", carreraJson, tokenAdmin);
        assertEquals(409, duplicada.statusCode());

        HttpResponse<String> ciclo1 = enviar("POST", "/api/v1/ciclos", """
                {"carreraId": %d, "numero": 1, "nombre": "Primer ciclo"}
                """.formatted(carreraId), tokenAdmin);
        assertEquals(201, ciclo1.statusCode());
        long ciclo1Id = id(ciclo1.body());

        HttpResponse<String> ciclo2 = enviar("POST", "/api/v1/ciclos", """
                {"carreraId": %d, "numero": 2, "nombre": "Segundo ciclo"}
                """.formatted(carreraId), tokenAdmin);
        assertEquals(201, ciclo2.statusCode());
        long ciclo2Id = id(ciclo2.body());

        HttpResponse<String> cursoBase = enviar("POST", "/api/v1/cursos", """
                {
                  "carreraId": %d,
                  "cicloId": %d,
                  "codigo": "MAT-101",
                  "nombre": "Matematica I",
                  "descripcion": "Fundamentos matematicos",
                  "creditos": 4,
                  "horasTeoria": 3,
                  "horasPractica": 2,
                  "prerequisitoIds": []
                }
                """.formatted(carreraId, ciclo1Id), tokenAdmin);
        assertEquals(201, cursoBase.statusCode());
        long cursoBaseId = id(cursoBase.body());

        HttpResponse<String> cursoAvanzado = enviar("POST", "/api/v1/cursos", """
                {
                  "carreraId": %d,
                  "cicloId": %d,
                  "codigo": "MAT-201",
                  "nombre": "Matematica II",
                  "descripcion": "Continuacion de Matematica I",
                  "creditos": 4,
                  "horasTeoria": 3,
                  "horasPractica": 2,
                  "prerequisitoIds": [%d]
                }
                """.formatted(carreraId, ciclo2Id, cursoBaseId), tokenAdmin);
        assertEquals(201, cursoAvanzado.statusCode());
        long cursoAvanzadoId = id(cursoAvanzado.body());
        assertTrue(cursoAvanzado.body().contains("MAT-101"));

        HttpResponse<String> aula = enviar("POST", "/api/v1/aulas", """
                {
                  "codigo": "LAB-01",
                  "nombre": "Laboratorio de Computo 1",
                  "tipo": "LABORATORIO",
                  "capacidad": 30,
                  "ubicacion": "Pabellon B - Segundo piso"
                }
                """, tokenAdmin);
        assertEquals(201, aula.statusCode());
        long aulaId = id(aula.body());

        HttpResponse<String> validacionCurso = enviar("GET",
                "/api/v1/cursos/" + cursoAvanzadoId + "/validacion", null, tokenEstudiante);
        assertEquals(200, validacionCurso.statusCode());
        assertTrue(validacionCurso.body().contains("\"activo\":true"));

        HttpResponse<String> aforoValido = enviar("GET",
                "/api/v1/aulas/" + aulaId + "/validacion?aforoRequerido=30", null, tokenAdmin);
        assertEquals(200, aforoValido.statusCode());
        assertTrue(aforoValido.body().contains("\"disponible\":true"));

        HttpResponse<String> aforoInsuficiente = enviar("GET",
                "/api/v1/aulas/" + aulaId + "/validacion?aforoRequerido=31", null, tokenAdmin);
        assertEquals(200, aforoInsuficiente.statusCode());
        assertTrue(aforoInsuficiente.body().contains("\"aforoSuficiente\":false"));

        HttpResponse<String> catalogo = enviar("GET", "/api/v1/catalogo", null, tokenEstudiante);
        assertEquals(200, catalogo.statusCode());
        assertTrue(catalogo.body().contains("MAT-201"));

        HttpResponse<String> prohibido = enviar("POST", "/api/v1/carreras", """
                {"codigo":"ADM","nombre":"Administracion","duracionCiclos":10}
                """, tokenEstudiante);
        assertEquals(403, prohibido.statusCode());

        HttpResponse<String> openApi = enviar("GET", "/v3/api-docs", null, null);
        assertEquals(200, openApi.statusCode());
        assertTrue(openApi.body().contains("API del Servicio de Cursos"));
    }

    private long id(String cuerpo) throws Exception {
        JsonNode json = objectMapper.readTree(cuerpo);
        return json.get("id").asLong();
    }

    private String generarToken(String correo, List<String> roles) {
        SecretKey clave = new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("servicio-usuarios")
                .subject(correo).issuedAt(ahora).expiresAt(ahora.plusSeconds(600))
                .claim("usuarioId", 1L).claim("roles", roles).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, String token)
            throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Accept", "application/json");
        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }
        if (cuerpo == null) {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            solicitud.header("Content-Type", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        }
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }
}
