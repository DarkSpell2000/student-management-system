package com.studentmanagement.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.jdbc.BasicDataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Factory
public class DataSourceConfig {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceConfig.class);
    
    @Singleton
    public DataSource dataSource(
            @Property(name = "datasources.default.url") String url,
            @Property(name = "datasources.default.username") String username,
            @Property(name = "datasources.default.password") String password,
            @Property(name = "datasources.default.driverClassName") String driverClassName) {
        
        LOG.info("Configuring DataSource...");
        LOG.info("URL: {}", url);
        LOG.info("Username: {}", username);
        LOG.info("Driver: {}", driverClassName);
        
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        
        // Оптимизация пула соединений
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        
        LOG.info("DataSource configured successfully!");
        return dataSource;
    }
}