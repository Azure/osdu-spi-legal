package org.opengroup.osdu.legal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "legaltag")
public class LegalTagConfig {
    private String expirationAlerts = "1m,2w,1d";
}
