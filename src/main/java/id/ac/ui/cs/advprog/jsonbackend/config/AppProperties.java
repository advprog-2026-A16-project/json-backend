package id.ac.ui.cs.advprog.jsonbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String frontendDev;
    private String frontendProd;
}