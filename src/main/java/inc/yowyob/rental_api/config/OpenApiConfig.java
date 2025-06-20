package inc.yowyob.rental_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080" + contextPath)
                    .description("Development Server"),
                new Server()
                    .url("https://api.rental.com" + contextPath)
                    .description("Production Server")
            ))
            .info(new Info()
                .title("Vehicle Rental API")
                .description("""
                            API complète pour la gestion de location de véhicules multi-agent.

                            ## Fonctionnalités principales :
                            - Gestion multi-tenant des organisations
                            - Système de forfaits d'abonnement
                            - Processus d'onboarding en 3 étapes
                            - Gestion des véhicules et chauffeurs
                            - Système de réservation intelligent
                            - Intégration paiements multiples
                            - Géofencing temps réel
                            - Chat intégré

                            ## Authentification :
                            Utilisez le token JWT dans l'en-tête Authorization: Bearer <token>
                            """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Équipe Développement")
                    .email("dev@rentalapi.com")
                    .url("https://github.com/your-repo"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Entrez votre token JWT")
                        .name("Authorization")
                        .in(SecurityScheme.In.HEADER)));
    }
}
