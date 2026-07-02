package in.indiemr.teleconsult.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
public class HibernateConfig {
    @Bean
    public LocalSessionFactoryBean localSessionFactoryBean(DataSource dataSource) {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("in.indiemr.teleconsult.model");

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "none"); // Liquibase owns schema
        props.put("hibernate.type.preferred_boolean_jdbc_type", "TINYINT");
        factoryBean.setHibernateProperties(props);
        return factoryBean;
    }

    @Bean
    public SessionFactory sessionFactory(LocalSessionFactoryBean factory) throws Exception {
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
