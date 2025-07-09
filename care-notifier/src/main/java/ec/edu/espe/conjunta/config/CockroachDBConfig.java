package ec.edu.espe.conjunta.config;

import ec.edu.espe.conjunta.repository.resilience.PendingNotificationRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
        basePackages = "ec.edu.espe.conjunta.repository", // Escanea este paquete y sus hijos
        entityManagerFactoryRef = "cockroachEntityManagerFactory",
        transactionManagerRef = "cockroachTransactionManager",
        // ¡¡CAMBIO CLAVE: AÑADIR FILTRO DE EXCLUSIÓN!!
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PendingNotificationRepository.class)
)
public class CockroachDBConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties cockroachDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "cockroachDataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource cockroachDataSource() {
        return cockroachDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "cockroachEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cockroachEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("cockroachDataSource") DataSource dataSource,
            JpaProperties jpaProperties) { // Inyectar JpaProperties

        var props = jpaProperties.getProperties();
        props.put("hibernate.hbm2ddl.auto", "update"); // Forzar DDL

        return builder
                .dataSource(dataSource)
                .packages("ec.edu.espe.conjunta.entity") // Paquete de Notification.java
                .persistenceUnit("cockroachPU")
                .properties(props)
                .build();
    }

    @Primary
    @Bean(name = "cockroachTransactionManager")
    public PlatformTransactionManager cockroachTransactionManager(
            @Qualifier("cockroachEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}