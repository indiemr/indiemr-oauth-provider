package in.indiemr.teleconsult.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ GoogleProperties.class, TeleconsultProperties.class })
public class GoogleConfig {
}