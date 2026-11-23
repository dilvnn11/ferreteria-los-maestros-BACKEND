package cl.ferreteria.losmaestros.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ferretería Los Maestros — API REST")
                        .description(
                                """
                                Backend de Ferretería Los Maestros (Spring Boot + MariaDB).

                                **Autenticación:** usar el botón Authorize con un Bearer JWT
                                obtenido en `POST /api/auth/login`.

                                **Roles:** administrador, vendedor, cliente.
                                Cuentas demo: admin@duoc.cl / Admin123, vendedor@duoc.cl / Vendedor123,
                                cliente@duoc.cl / Cliente123.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Ferretería Los Maestros")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido en /api/auth/login")));
    }
}
