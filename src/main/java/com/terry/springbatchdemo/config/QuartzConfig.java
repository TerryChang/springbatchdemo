package com.terry.springbatchdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Slf4j
public class QuartzConfig {

    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource(@Value("${spring.quartz.datasource.url}") String url
                                                , @Value("${spring.quartz.datasource.username}") String username
                                                , @Value("${spring.quartz.datasource.password}") String password
                                                , @Value("${spring.quartz.datasource.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext, QuartzProperties quartzProperties) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactoryBean.setJobFactory(jobFactory);

        schedulerFactoryBean.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        // schedulerFactoryBean.setGlobalTriggerListeners(triggersListener);
        // schedulerFactoryBean.setGlobalJobListeners(jobsListener);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        // datasource 와 관련된 bean 이름은 spring.quartz.properties.org.quartz.jobstore.datasource에 설정해놓았음(https://www.baeldung.com/spring-quartz-schedule 참조)
        // schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        return schedulerFactoryBean;

    }
}
