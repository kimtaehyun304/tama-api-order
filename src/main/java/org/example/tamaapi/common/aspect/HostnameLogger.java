package org.example.tamaapi.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Aspect
@Component
@Slf4j
public class HostnameLogger {

    @Autowired
    private DataSource dataSource;

    //@After("execution(* org.example.tamaapi.query..*(..))")
    public void logDbHost() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@hostname")) {
            if (rs.next()) {
                log.info("Connected DB Host: {}", rs.getString(1));
            }
        }
    }
}