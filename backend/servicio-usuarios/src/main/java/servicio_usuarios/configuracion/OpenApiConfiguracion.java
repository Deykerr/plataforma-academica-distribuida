package servicio_usuarios.configuracion;

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
    OpenAPI usuariosOpenApi() {
        String esquema = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("API del Servicio de Usuarios")
                        .version("v1")
                        .description("Identidad, estudiantes, docentes, roles y validacion para la plataforma academica distribuida.")
                        .contact(new Contact().name("Proyecto Sistemas Distribuidos")))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().name(esquema).type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }
}
