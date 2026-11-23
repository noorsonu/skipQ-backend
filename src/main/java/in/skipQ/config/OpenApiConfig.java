package in.skipQ.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skipQOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token directly or with 'Bearer ' prefix.")))
                .info(new Info()
                        .title("SkipQ API Documentation")
                        .description("SkipQ - Hyperlocal Live Availability & Virtual Queue Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SkipQ Team")
                                .url("https://skipq.in")
                                .email("support@skipq.in"))
                        .license(new License().name("Internal - Confidential")));
    }
}

