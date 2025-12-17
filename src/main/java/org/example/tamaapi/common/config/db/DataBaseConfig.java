package org.example.tamaapi.common.config.db;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.example.tamaapi.common.config.db.RoundRobinDataSource.DATASOURCE_KEY_MASTER;


@Configuration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DataBaseConfig {

    @Autowired
    private DataBaseProperty dataBaseProperty;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public List<DataSource> slaveDataSources() {
        List<DataSource> sources = new ArrayList<>();

        //yml 변동에 따라 자동으로 추가하기 위함
        for (DataBaseProperty.DatabaseDetail slave : dataBaseProperty.getSlaves()) {
            sources.add(
                    DataSourceBuilder.create()
                            .type(HikariDataSource.class)
                            .url(slave.getJdbcUrl())
                            .username(slave.getUsername())
                            .password(slave.getPassword())
                            .driverClassName(slave.getDriverClassName())
                            .build()
            );
        }

        return sources;
    }

    @Bean
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource master,
                                        @Qualifier("slaveDataSources") List<DataSource> slaves) {
        //RoundRobinDataSource dataSource = new RoundRobinDataSource(dataBaseProperty.getSlaves().size());
        RoutingDataSource dataSource = new RoutingDataSource();
        HashMap<Object, Object> sources = new HashMap<>();
        sources.put(DATASOURCE_KEY_MASTER, master);

        //HashMap에 slave DataSource put하기
        int index = 0;
        for (DataSource slave : slaves) {
            sources.put("slave"+index, slave);
            ++index;
        }

        dataSource.setTargetDataSources(sources);
        dataSource.setDefaultTargetDataSource(master);
        return dataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

}
