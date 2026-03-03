package id.ac.ui.cs.advprog.jsonbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private Environment env;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        String activeProfile = Arrays.stream(env.getActiveProfiles()).findFirst().orElse("dev");

        if ("prod".equals(activeProfile)) {
            config.setAllowedOrigins(Arrays.asList(appProperties.getFrontendProd()));
        } else {
            config.setAllowedOrigins(Arrays.asList(appProperties.getFrontendDev()));
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}