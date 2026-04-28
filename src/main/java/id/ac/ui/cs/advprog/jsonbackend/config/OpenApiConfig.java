package id.ac.ui.cs.advprog.jsonbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jsonOpenApi() {
        String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("JSON Backend API")
                        .description("""
                                API documentation for JaStip Online Nasional backend.

                                Inventory module uses outbox-based event flow for stock mutation events:
                                - STOCK_RESERVED
                                - STOCK_RELEASED

                                Outbox lifecycle:
                                PENDING -> SENT
                                PENDING -> FAILED -> PENDING (retryable)
                                FAILED -> DEAD_LETTER (retry limit reached)
                                PENDING -> DEAD_LETTER (non-retryable failure)
                                """)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
