package com.sparta.settlementservice.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.sparta.settlementservice.batch.repo.master",
                "com.sparta.settlementservice.streaming",
                "com.sparta.settlementservice.user"},
        entityManagerFactoryRef = "masterEntityManagerFactory",
        transactionManagerRef = "masterTransactionManager"
)

public class MasterDBConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public HikariDataSource masterDBSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();

        // 커넥션 풀 크기 30으로 설정
        dataSource.setMaximumPoolSize(100);
        // 30초 동안 사용되지 않은 커넥션 자동 종료
        dataSource.setIdleTimeout(30000);
        return dataSource;
    }


    @Primary
    @Bean
    public PlatformTransactionManager masterTransactionManager() {

        return new DataSourceTransactionManager(masterDBSource());
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory() {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(masterDBSource());
        em.setPackagesToScan("com.sparta.settlementservice");  // entity scan 폴더
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return em;
    }

}
