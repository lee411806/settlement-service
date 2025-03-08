package com.sparta.settlementservice.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.sparta.settlementservice.batch.repo.slave",
        entityManagerFactoryRef = "slaveEntityManagerFactory",
        transactionManagerRef = "slaveTransactionManager"
)
public class SlaveDBConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.slave") //  Slave DB 설정
    public DataSource slaveDBSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager slaveTransactionManager() {

        return new DataSourceTransactionManager(slaveDBSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean slaveEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(slaveDBSource());
        em.setPackagesToScan("com.sparta.settlementservice"); // 원하는 entity 폴더 스캔
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        //  Hibernate 설정 추가 (YML에서 자동 적용되지 않음)
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto", "none");  //  Slave는 스키마 변경 금지
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        em.setJpaProperties(properties);

        return em;
    }


}
