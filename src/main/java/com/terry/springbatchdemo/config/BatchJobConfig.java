package com.terry.springbatchdemo.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.terry.springbatchdemo.config.json.*;
import com.terry.springbatchdemo.entity.Product;
import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.entity.ShoppingItem;
import com.terry.springbatchdemo.entity.User;
import com.terry.springbatchdemo.repository.ProductRepository;
import com.terry.springbatchdemo.repository.ShoppingCartRepository;
import com.terry.springbatchdemo.repository.ShoppingItemRepository;
import com.terry.springbatchdemo.repository.UserRepository;
import com.terry.springbatchdemo.vo.LineResultVO;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import com.terry.springbatchdemo.vo.ShoppingItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @EnableBatchProcessing 어노테이션을 Spring Boot의 Application 클래스에 명시할경우 @DataJpaTest와 문제를 일으키는 상황이 있다(@DataJpaTest 관련 모든 테스트 클래스에서 flush 하는 과정에 문제가 발생)
 * 그래서 batch 환경 설정 클래스에 이 어노테이션을 추가했다
 */
@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchJobConfig {
    private final String filePath;
    private final String fileNamePrefix;
    private final String fileExt;
    private final EntityManagerFactory entityManagerFactory;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    public BatchJobConfig(@Value("${logfile.path}") String filePath
                            , @Value("${logfile.fileNamePrefix}") String fileNamePrefix
                            ,  @Value("${logfile.fileExt}") String fileExt
                            , EntityManagerFactory entityManagerFactory
                            , UserRepository userRepository
                            , ProductRepository productRepository
                            , ShoppingItemRepository shoppingItemRepository
                            , ShoppingCartRepository shoppingCartRepository) {
        this.filePath = filePath;
        this.fileNamePrefix = fileNamePrefix;
        this.fileExt = fileExt;
        this.entityManagerFactory = entityManagerFactory;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    /**
     * Custom Json Serializer 나 Custom Json Deserializer 를 등록하기 위해 ObjectMapper Bean을 별도로 생성한다
     * Spring Boot 에서는 자체적으로 ObjectMapper 를 Bean 으로 생성하지 않고 있기 때문에 이 작업이 가능하다
     * 이것을 확인할 수 있는 방법은 클래스의 Member 변수로 ObjectMapper 타입 변수를 하나 선언한뒤
     * 생성자에서 이를 injection 받게 해주면 Spring이 ObjectMapper를 bean으로 생성했다면 injection이 이루어 질 것이고
     * 그렇지 않으면 injection에 실패할 것이기 때문에 알 수 있다
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ProductVO.class, new ProductVODeserializer(objectMapper));
        simpleModule.addDeserializer(ShoppingItemVO.class, new ShoppingItemVODeserializer(objectMapper));
        simpleModule.addDeserializer(ShoppingCartVO.class, new ShoppingCartVODeserializer(objectMapper));
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }

    @Bean
    @StepScope
    public DataShareBean dataShareBean() {
        return new DataShareBean();
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
    public Step readFileWriteDatabaseStep(StepBuilderFactory stepBuilderFactory, ObjectMapper objectMapper, DataShareBean dataShareBean) throws Exception {
        return stepBuilderFactory.get("readFileWriteDatabaseStep")
                .<ShoppingCartVO, ShoppingCart>chunk(10)// chunk 메소드에 반드시 작업할 타입을 명시해주어야 한다. 그러지 않으면 processor 메소드에서 해당 작업을 하기 위해 만들어 놓은 메소드를 찾질 못한다(chunk 메소드에 타입을 명시하지 않으면 <Object, Object>로 인식하기 때문이다
                .reader(shoppingCartFlatFileItemReader(objectMapper, dataShareBean))
                .processor(customItemProcessor())
                // .writer(shoppingCartJpaItemWriter())
                .writer(customShoppingCartJpaItemWriter())
                .faultTolerant()
                .skipLimit(10)
                .skip(BatchException.class)
                .skip(JsonParseException.class)
                .skip(JsonMappingException.class)
                // .writer(shoppingCartDebugItemWriter())
                // .writer(shoppingCartLoggingItemWriter())
                .build();

        // reader에서 발생할수 있는 예외
        // IOException, JsonParseException, JsonMappingException
    }

    @Bean
    @StepScope
    public FlatFileItemReader<ShoppingCartVO> shoppingCartFlatFileItemReader(ObjectMapper objectMapper, DataShareBean dataShareBean) {
        String todayString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String todayString2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String filePathName = filePath + "/" + fileNamePrefix + todayString + "." + fileExt;
        // LineMapper<LineResultVO> lineResultVOLineMapper = new LineResultVOLineMapper(objectMapper, 15, -1);
        LineMapper<ShoppingCartVO> ShoppingCartVOLineMapper = new ShoppingCartVOLineMapper(objectMapper, 15, -1, dataShareBean);

        return new FlatFileItemReaderBuilder<ShoppingCartVO>()
                .lineMapper(ShoppingCartVOLineMapper)
                .encoding("UTF-8")
                .resource(new FileSystemResource(filePathName))
                .name("shoppingCartJsonItemReader")
                .build();
    }
    /*
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
    */

    @Bean
    @StepScope
    public CustomItemProcessor customItemProcessor(DataShareBean dataShareBean) {
        return new CustomItemProcessor(userRepository, productRepository, dataShareBean);
    }

    @Bean
    @StepScope
    public JpaItemWriter<ShoppingCart> shoppingCartJpaItemWriter() {
        JpaItemWriter<ShoppingCart> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    @StepScope
    public CustomShoppingCartJpaItemWriter customShoppingCartJpaItemWriter(DataShareBean dataShareBean) {
        CustomShoppingCartJpaItemWriter customShoppingCartJpaItemWriter = new CustomShoppingCartJpaItemWriter();
        customShoppingCartJpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return customShoppingCartJpaItemWriter;
    }

    @Bean
    @StepScope
    public LoggingItemWriter<ShoppingCart> shoppingCartLoggingItemWriter() {
        return new LoggingItemWriter<>("ShoppingCart : {}");
    }

    private ItemWriter<ShoppingCart> shoppingCartDebugItemWriter() {
        return list -> {
            for(ShoppingCart shoppingCart : list) {
                logger.info("ShoppingCart : {}", shoppingCart);
            }
        };
    }
}
