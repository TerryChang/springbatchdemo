package com.terry.springbatchdemo.config;

import com.terry.springbatchdemo.entity.ShoppingCart;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @EnableBatchProcessing 어노테이션을 Spring Boot의 Application 클래스에 명시할경우 @DataJpaTest와 문제를 일으키는 상황이 있다(@DataJpaTest 관련 모든 테스트 클래스에서 flush 하는 과정에 문제가 발생)
 * 그래서 batch 환경 설정 클래스에 이 어노테이션을 추가했다
 */
@Configuration
@EnableBatchProcessing
public class BatchJobConfig {
    private final String filePath;
    private final String fileNamePrefix;
    private final String fileExt;
    private final EntityManagerFactory entityManagerFactory;

    public BatchJobConfig() {
        this.filePath = null;
        this.fileNamePrefix = null;
        this.fileExt = null;
        this.entityManagerFactory = null;
    }

    @Autowired
    public BatchJobConfig(@Value("${logfile.path}") String filePath, @Value("${logfile.fileNamePrefix}") String fileNamePrefix,  @Value("${logfile.fileExt}") String fileExt, EntityManagerFactory entityManagerFactory) {
        this.filePath = filePath;
        this.fileNamePrefix = fileNamePrefix;
        this.fileExt = fileExt;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job readFileWriteDatabaseJob(JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("readFileWriteDatabaseJob")
                .preventRestart()
                .start(step)
                .build();
    }
    @Bean
    @JobScope
    public Step readFileWriteDatabaseStep(StepBuilderFactory stepBuilderFactory) throws Exception {
        return stepBuilderFactory.get("readFileWriteDatabaseStep")
                .<ShoppingCart, ShoppingCart>chunk(10)// chunk 메소드에 반드시 작업할 타입을 명시해주어야 한다. 그러지 않으면 processor 메소드에서 해당 작업을 하기 위해 만들어 놓은 메소드를 찾질 못한다(chunk 메소드에 타입을 명시하지 않으면 <Object, Object>로 인식하기 때문이다
                .reader(shoppingCartJsonItemReader())
                .processor(shoppingCartItemProcessor())
                .writer(shoppingCartJpaItemWriter())
                .build();
    }

    @Bean
    public JsonItemReader<ShoppingCart> shoppingCartJsonItemReader() throws Exception{
        String todayString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String todayString2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String filePathName = filePath + "/" + fileNamePrefix + todayString + "." + fileExt;
        return new JsonItemReaderBuilder<ShoppingCart>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(ShoppingCart.class))
                .resource(new FileSystemResource(filePathName))
                .name("shoppingCartJsonItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<ShoppingCart, ShoppingCart> shoppingCartItemProcessor() {
        /*
        return new ItemProcessor<ShoppingCart, ShoppingCart>() {
            @Override
            public ShoppingCart process(ShoppingCart item) throws Exception {
                return item;
            }
        };
        */
        return  item -> item;
    }

    @Bean
    public JpaItemWriter<ShoppingCart> shoppingCartJpaItemWriter() {
        JpaItemWriter<ShoppingCart> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
