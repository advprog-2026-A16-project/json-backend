package id.ac.ui.cs.advprog.jsonbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        List<String> devOrigins = new ArrayList<>(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://127.0.2.2:*",
                "https://*.herokuapp.com"
        ));
        if (StringUtils.hasText(appProperties.getFrontendDev())) {
            devOrigins.add(appProperties.getFrontendDev());
        }
        if (StringUtils.hasText(appProperties.getFrontendProd())) {
            devOrigins.add(appProperties.getFrontendProd());
        }

        if ("prod".equals(activeProfile)) {
            if (StringUtils.hasText(appProperties.getFrontendProd())) {
                config.setAllowedOriginPatterns(Arrays.asList(appProperties.getFrontendProd()));
            } else {
                config.setAllowedOriginPatterns(Arrays.asList("https://*.herokuapp.com"));
            }
        } else {
            config.setAllowedOriginPatterns(devOrigins);
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
