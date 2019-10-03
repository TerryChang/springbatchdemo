package com.terry.springbatchdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
// @Profile("h2")
@Slf4j
public class H2Config {

    /*
    @Bean(name = "h2Server", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnExpression("${h2.tcpServer.enabled:false}")
    public Server createTcpServer(@Value("${h2.tcpServer.port:9092}") String h2TcpPort) throws SQLException {
        logger.info("H2 Server Start");
        Server h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
        return h2Server;
    }
     */

    /*
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource h2DataSource() throws SQLException {
        logger.info("DataSource Settings start");
        return new com.zaxxer.hikari.HikariDataSource();
    }
     */

}
