package com.terry.springbatchdemo.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

@Configuration
@Profile("h2")
public class H2Config {

    @Bean(name = "h2Server", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnExpression("${h2.tcpServer.enabled:false}")
    public Server createTcpServer(@Value("${h2.tcpServer.port:9092}") String h2TcpPort) throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
    }
}
