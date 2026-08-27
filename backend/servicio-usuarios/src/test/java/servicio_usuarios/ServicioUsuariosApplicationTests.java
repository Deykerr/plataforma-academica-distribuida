package servicio_usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicioUsuariosApplicationTests {

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void flujoPrincipalDeEstudiantesYSwagger() throws Exception {
        String estudianteJson = """
                {
                  "correo": "alumna@academica.test",
                  "clave": "Clave123*",
                  "codigo": "20260001",
                  "nombres": "Ana Maria",
                  "apellidos": "Torres Quispe",
                  "documentoIdentidad": "76543210",
                  "fechaNacimiento": "2002-05-20",
                  "telefono": "+51 999 888 777",
                  "direccion": "Ayacucho",
                  "carreraId": 1
                }
                """;

        HttpResponse<String> registro = enviar("POST", "/api/v1/estudiantes", estudianteJson, null);
        assertEquals(201, registro.statusCode());
        assertTrue(registro.body().contains("20260001"));

        HttpResponse<String> duplicado = enviar("POST", "/api/v1/estudiantes", estudianteJson, null);
        assertEquals(409, duplicado.statusCode());

        String tokenEstudiante = iniciarSesion("alumna@academica.test", "Clave123*");
        HttpResponse<String> perfil = enviar("GET", "/api/v1/estudiantes/me", null, tokenEstudiante);
        assertEquals(200, perfil.statusCode());
        assertTrue(perfil.body().contains("Ana Maria"));

        HttpResponse<String> listaProhibida = enviar("GET", "/api/v1/estudiantes", null, tokenEstudiante);
        assertEquals(403, listaProhibida.statusCode());

        String tokenAdmin = iniciarSesion("admin@academica.test", "Admin123*");
        HttpResponse<String> listaAdmin = enviar("GET", "/api/v1/estudiantes", null, tokenAdmin);
        assertEquals(200, listaAdmin.statusCode());
        assertTrue(listaAdmin.body().contains("alumna@academica.test"));

        HttpResponse<String> openApi = enviar("GET", "/v3/api-docs", null, null);
        assertEquals(200, openApi.statusCode());
        assertTrue(openApi.body().contains("API del Servicio de Usuarios"));
    }

    private String iniciarSesion(String correo, String clave) throws Exception {
        String cuerpo = "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}";
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/auth/login", cuerpo, null);
        assertEquals(200, respuesta.statusCode());
        JsonNode json = objectMapper.readTree(respuesta.body());
        return json.get("token").asText();
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
