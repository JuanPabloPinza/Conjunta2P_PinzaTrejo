package ec.edu.espe.conjunta.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties; // ¡IMPORTAR ESTA CLASE!
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ec.edu.espe.conjunta.repository.resilience",
        entityManagerFactoryRef = "resilienceEntityManagerFactory",
        transactionManagerRef = "resilienceTransactionManager"
)
public class ResilienceDbConfig {

    @Bean
    @ConfigurationProperties("spring.resilience-datasource")
    public DataSourceProperties resilienceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "resilienceDataSource")
    @ConfigurationProperties("spring.resilience-datasource.hikari")
    public DataSource resilienceDataSource() {
        return resilienceDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "resilienceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean resilienceEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("resilienceDataSource") DataSource dataSource,
            // Inyectamos el bean JpaProperties que Spring Boot ya creó para nosotros
            JpaProperties jpaProperties) {

        return builder
                .dataSource(dataSource)
                .packages("ec.edu.espe.conjunta.entity.resilience")
                .persistenceUnit("resiliencePU")
                // ¡¡CAMBIO CLAVE: APLICAMOS LAS PROPIEDADES JPA (ddl-auto, dialect, etc.)!!
                .properties(jpaProperties.getProperties())
                .build();
    }

    @Bean(name = "resilienceTransactionManager")
    public PlatformTransactionManager resilienceTransactionManager(
            @Qualifier("resilienceEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}