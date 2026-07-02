package in.indiemr.teleconsult;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import in.indiemr.teleconsult.config.GoogleProperties;
import in.indiemr.teleconsult.config.TeleconsultProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@EnableConfigurationProperties({ GoogleProperties.class, TeleconsultProperties.class })
public class TeleconsultApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeleconsultApplication.class, args);
    }
}