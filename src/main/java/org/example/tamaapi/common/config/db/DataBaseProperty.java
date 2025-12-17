package org.example.tamaapi.common.config.db;


import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DataBaseProperty {

    private DatabaseDetail master;
    private List<DatabaseDetail> slaves;

    @Data
    public static class DatabaseDetail {
        private String driverClassName;
        private String jdbcUrl;
        private String username;
        private String password;
    }

}
