package com.smartcampus.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DatabaseManager {

    private static final HikariDataSource DATA_SOURCE = init();

    private DatabaseManager() {}

    private static HikariDataSource init() {
        String url  = getEnvOrDefault("DB_URL",      "jdbc:postgresql://localhost:5432/smartcampus");
        String user = getEnvOrDefault("DB_USER",     "smartcampus_user");
        String pass = getEnvOrDefault("DB_PASSWORD", "smartcampus_pass");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        HikariDataSource ds = new HikariDataSource(config);

        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        return ds;
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    public static java.sql.Connection getConnection() throws java.sql.SQLException {
        return DATA_SOURCE.getConnection();
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}