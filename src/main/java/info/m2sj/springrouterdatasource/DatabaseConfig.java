package info.m2sj.springrouterdatasource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource createRouterDatasource() {
        AbstractRoutingDataSource routingDataSource = new MyRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put("current:db01", createDataSource("jdbc:mysql://localhost:3306/test_db02",
                "test", "1234"));

        targetDataSources.put("current:db02", createDataSource("jdbc:mysql://localhost:3306/test_db01",
                "test", "1234"));


        routingDataSource.setTargetDataSources(targetDataSources);

        return routingDataSource;
    }

    private DataSource createDataSource(String url, String user, String password) {
        com.zaxxer.hikari.HikariDataSource dataSource = new com.zaxxer.hikari.HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        return dataSource;
    }
}
