package servicio_matriculas;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import servicio_matriculas.dto.integracion.AulaValidacion;
import servicio_matriculas.dto.integracion.CursoValidacion;
import servicio_matriculas.dto.integracion.UsuarioValidacion;
import servicio_matriculas.dto.integracion.ValidacionPrerrequisitos;
import servicio_matriculas.integracion.IntegracionAcademicaCliente;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicioMatriculasApplicationTests {

    private static final String SECRETO = "clave-super-segura-para-pruebas-automatizadas-123456";

    @Value("${local.server.port}")
    private int puerto;

    @MockitoBean
    private IntegracionAcademicaCliente integracion;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void flujoCompletoDeMatriculaSeguridadCuposYReportes() throws Exception {
        prepararIntegraciones();
        assertEquals(401, enviar("GET", "/api/v1/periodos", null, null).statusCode());

        String tokenAdmin = generarToken(1L, "admin@academica.test", List.of("ADMINISTRADOR"));
        String tokenEstudiante = generarToken(100L, "estudiante@academica.test", List.of("ESTUDIANTE"));
        String tokenOtro = generarToken(101L, "otro@academica.test", List.of("ESTUDIANTE"));
        String tokenDocente = generarToken(200L, "docente@academica.test", List.of("DOCENTE"));
        String tokenDocenteAjeno = generarToken(201L, "docente-ajeno@academica.test",
                List.of("DOCENTE"));
        String tokenSinPrerrequisito = generarToken(102L, "sin-prerrequisito@academica.test",
                List.of("ESTUDIANTE"));
        LocalDate hoy = LocalDate.now();

        HttpResponse<String> periodo = enviar("POST", "/api/v1/periodos", """
                {
                  "codigo":"2026-II",
                  "nombre":"Semestre 2026-II",
                  "fechaInicio":"%s",
                  "fechaFin":"%s",
                  "fechaInicioMatricula":"%s",
                  "fechaFinMatricula":"%s"
                }
                """.formatted(hoy.plusDays(2), hoy.plusDays(120), hoy.minusDays(1), hoy.plusDays(1)),
                tokenAdmin);
        assertEquals(201, periodo.statusCode());
        long periodoId = id(periodo.body());

        HttpResponse<String> duplicado = enviar("POST", "/api/v1/periodos", """
                {
                  "codigo":"2026-II",
                  "nombre":"Duplicado",
                  "fechaInicio":"%s",
                  "fechaFin":"%s",
                  "fechaInicioMatricula":"%s",
                  "fechaFinMatricula":"%s"
                }
                """.formatted(hoy.plusDays(2), hoy.plusDays(120), hoy.minusDays(1), hoy.plusDays(1)),
                tokenAdmin);
        assertEquals(409, duplicado.statusCode());

        HttpResponse<String> seccion = enviar("POST", "/api/v1/secciones", """
                {
                  "periodoId":%d,
                  "cursoId":10,
                  "aulaId":20,
                  "docenteId":200,
                  "codigo":"A",
                  "capacidad":1,
                  "horarios":[
                    {"diaSemana":"LUNES","horaInicio":"08:00:00","horaFin":"10:00:00"}
                  ]
                }
                """.formatted(periodoId), tokenAdmin);
        assertEquals(201, seccion.statusCode());
        long seccionId = id(seccion.body());

        HttpResponse<String> choqueAula = enviar("POST", "/api/v1/secciones", """
                {
                  "periodoId":%d,
                  "cursoId":11,
                  "aulaId":20,
                  "docenteId":201,
                  "codigo":"B",
                  "capacidad":20,
                  "horarios":[
                    {"diaSemana":"LUNES","horaInicio":"09:00:00","horaFin":"11:00:00"}
                  ]
                }
                """.formatted(periodoId), tokenAdmin);
        assertEquals(409, choqueAula.statusCode());

        assertEquals(200, enviar("PATCH", "/api/v1/periodos/" + periodoId + "/estado",
                "{\"estado\":\"MATRICULA_ABIERTA\"}", tokenAdmin).statusCode());
        assertEquals(200, enviar("PATCH", "/api/v1/secciones/" + seccionId + "/estado",
                "{\"estado\":\"ABIERTA\"}", tokenAdmin).statusCode());

        HttpResponse<String> sinPrerrequisito = enviar("POST", "/api/v1/matriculas",
                "{\"estudianteId\":102,\"seccionId\":" + seccionId + "}",
                tokenSinPrerrequisito);
        assertEquals(400, sinPrerrequisito.statusCode());
        assertTrue(sinPrerrequisito.body().contains("50"));

        HttpResponse<String> matricula = enviar("POST", "/api/v1/matriculas",
                "{\"estudianteId\":100,\"seccionId\":" + seccionId + "}", tokenEstudiante);
        assertEquals(201, matricula.statusCode());
        long matriculaId = id(matricula.body());

        HttpResponse<String> listadoDocente = enviar("GET",
                "/api/v1/matriculas/seccion/" + seccionId, null, tokenDocente);
        assertEquals(200, listadoDocente.statusCode());
        assertTrue(listadoDocente.body().contains("\"estudianteId\":100"));
        assertEquals(403, enviar("GET", "/api/v1/matriculas/seccion/" + seccionId,
                null, tokenDocenteAjeno).statusCode());

        assertEquals(409, enviar("POST", "/api/v1/matriculas",
                "{\"estudianteId\":100,\"seccionId\":" + seccionId + "}",
                tokenEstudiante).statusCode());
        assertEquals(409, enviar("POST", "/api/v1/matriculas",
                "{\"estudianteId\":101,\"seccionId\":" + seccionId + "}", tokenOtro).statusCode());

        HttpResponse<String> mias = enviar("GET", "/api/v1/matriculas/mias", null, tokenEstudiante);
        assertEquals(200, mias.statusCode());
        assertTrue(mias.body().contains("\"estudianteId\":100"));
        assertEquals(403, enviar("GET", "/api/v1/matriculas", null, tokenEstudiante).statusCode());

        HttpResponse<String> validacion = enviar("GET",
                "/api/v1/matriculas/" + matriculaId + "/validacion", null, tokenAdmin);
        assertEquals(200, validacion.statusCode());
        assertTrue(validacion.body().contains("\"activa\":true"));

        HttpResponse<String> resumen = enviar("GET",
                "/api/v1/reportes/periodos/" + periodoId + "/resumen", null, tokenAdmin);
        assertEquals(200, resumen.statusCode());
        assertTrue(resumen.body().contains("\"matriculasActivas\":1"));
        assertTrue(resumen.body().contains("\"vacantesDisponibles\":0"));

        HttpResponse<String> retiro = enviar("PATCH", "/api/v1/matriculas/" + matriculaId
                + "/retiro", "{\"motivo\":\"Cambio de horario\"}", tokenEstudiante);
        assertEquals(200, retiro.statusCode());
        assertTrue(retiro.body().contains("\"estado\":\"RETIRADA\""));

        assertEquals(201, enviar("POST", "/api/v1/matriculas",
                "{\"estudianteId\":101,\"seccionId\":" + seccionId + "}", tokenOtro).statusCode());
        assertEquals(403, enviar("POST", "/api/v1/periodos", """
                {
                  "codigo":"2027-I",
                  "nombre":"Semestre sin permiso",
                  "fechaInicio":"%s",
                  "fechaFin":"%s",
                  "fechaInicioMatricula":"%s",
                  "fechaFinMatricula":"%s"
                }
                """.formatted(hoy.plusDays(130), hoy.plusDays(240), hoy.plusDays(100),
                        hoy.plusDays(120)), tokenEstudiante).statusCode());

        HttpResponse<String> openApi = enviar("GET", "/v3/api-docs", null, null);
        assertEquals(200, openApi.statusCode());
        assertTrue(openApi.body().contains("API del Servicio de Matriculas"));
    }

    private void prepararIntegraciones() {
        when(integracion.validarUsuario(anyLong(), eq("ESTUDIANTE"))).thenAnswer(invocacion -> {
            Long id = invocacion.getArgument(0);
            return new UsuarioValidacion(id, true, true, "ACTIVO", Set.of("ESTUDIANTE"));
        });
        when(integracion.validarUsuario(anyLong(), eq("DOCENTE"))).thenAnswer(invocacion -> {
            Long id = invocacion.getArgument(0);
            return new UsuarioValidacion(id, true, true, "ACTIVO", Set.of("DOCENTE"));
        });
        when(integracion.validarCurso(anyLong())).thenAnswer(invocacion -> {
            Long id = invocacion.getArgument(0);
            return new CursoValidacion(id, true, true, 1L, 1L, 4, Set.of(50L));
        });
        when(integracion.validarPrerrequisitos(anyLong(), anySet())).thenAnswer(invocacion -> {
            Long estudianteId = invocacion.getArgument(0);
            if (estudianteId.equals(102L)) {
                return new ValidacionPrerrequisitos(estudianteId, false, Set.of(), Set.of(50L));
            }
            return new ValidacionPrerrequisitos(estudianteId, true, Set.of(50L), Set.of());
        });
        when(integracion.validarAula(anyLong(), anyInt())).thenAnswer(invocacion -> {
            Long id = invocacion.getArgument(0);
            Integer aforo = invocacion.getArgument(1);
            return new AulaValidacion(id, true, aforo <= 40, aforo <= 40, 40,
                    "AULA", "DISPONIBLE");
        });
    }

    private long id(String cuerpo) throws Exception {
        JsonNode json = objectMapper.readTree(cuerpo);
        return json.get("id").asLong();
    }

    private String generarToken(Long usuarioId, String correo, List<String> roles) {
        SecretKey clave = new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("servicio-usuarios")
                .subject(correo).issuedAt(ahora).expiresAt(ahora.plusSeconds(600))
                .claim("usuarioId", usuarioId).claim("roles", roles).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, String token)
            throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta)).header("Accept", "application/json");
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
