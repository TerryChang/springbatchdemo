package com.terry.springbatchdemo.config;

import com.zaxxer.hikari.HikariDataSource;
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
@Profile({"h2", "h2_log4jdbc"})
@Slf4j
public class H2Config {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() throws SQLException {
        Server server = persistenceRun(9092, "springbatch", "batchdb", DBFilePath.relative);
        // Server server = memoryRun(9092);
        if(server.isRunning(true)){
            logger.info("server run success");
        }
        logger.info("h2 server url = {}", server.getURL());

        return new HikariDataSource();
    }

    private Server persistenceRun(int port, String dbName, String dbFileName, DBFilePath dbFilePath) throws SQLException {
        return Server.createTcpServer(
                "-tcp",
                "-tcpAllowOthers",
                "-ifNotExists",
                "-tcpPort", port+"", "-key", dbName, dbFilePath.dbFilePathValue(dbFileName)).start();
    }

    private Server memoryRun(int port) throws SQLException {
        return Server.createTcpServer(
                "-tcp",
                "-tcpAllowOthers",
                "-ifNotExists",
                "-tcpPort", port+"").start();
    }

    enum DBFilePath {
        absolute("~/"), relative("./");
        String prefix;

        DBFilePath(String prefix) {
            this.prefix = prefix;
        }

        public String dbFilePathValue(String dbFileName) {
            return prefix + dbFileName;
        }
    }
}
