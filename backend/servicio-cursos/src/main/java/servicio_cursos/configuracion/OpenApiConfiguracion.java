package servicio_cursos.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguracion {

    @Bean
    OpenAPI cursosOpenApi() {
        String esquema = "bearer-jwt";
        return new OpenAPI()
                .info(new Info().title("API del Servicio de Cursos").version("v1")
                        .description("Carreras, ciclos, cursos, prerrequisitos, aulas y catalogo academico.")
                        .contact(new Contact().name("Proyecto Sistemas Distribuidos")))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().name(esquema).type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }
}
