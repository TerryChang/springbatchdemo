package com.terry.springbatchdemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.terry.springbatchdemo.config.json.ProductVODeserializer;
import com.terry.springbatchdemo.config.json.ShoppingCartVODeserializer;
import com.terry.springbatchdemo.config.json.ShoppingCartVOLineMapper;
import com.terry.springbatchdemo.config.json.ShoppingItemVODeserializer;
import com.terry.springbatchdemo.config.listener.ChunkListenerImpl;
import com.terry.springbatchdemo.config.listener.CustomSkipListener;
import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.repository.ProductRepository;
import com.terry.springbatchdemo.repository.ShoppingCartRepository;
import com.terry.springbatchdemo.repository.ShoppingItemRepository;
import com.terry.springbatchdemo.repository.UserRepository;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import com.terry.springbatchdemo.vo.ShoppingItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
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
        /*
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ProductVO.class, new ProductVODeserializer(objectMapper));
        simpleModule.addDeserializer(ShoppingItemVO.class, new ShoppingItemVODeserializer(objectMapper));
        simpleModule.addDeserializer(ShoppingCartVO.class, new ShoppingCartVODeserializer(objectMapper));
        objectMapper.registerModule(simpleModule);
        */

        return objectMapper;
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
    public Step readFileWriteDatabaseStep(StepBuilderFactory stepBuilderFactory
            , FlatFileItemReader<ShoppingCartVO> shoppingCartFlatFileItemReader
            , CustomItemProcessor customItemProcessor
            // , CustomShoppingCartJpaItemWriter customShoppingCartJpaItemWriter
            , JpaItemWriter<ShoppingCart> jpaItemWriter
            , ChunkListenerImpl chunkListenerImpl
            , CustomSkipListener customSkipListener) throws Exception {
        return stepBuilderFactory.get("readFileWriteDatabaseStep")
                .<ShoppingCartVO, ShoppingCart>chunk(10)// chunk 메소드에 반드시 작업할 타입을 명시해주어야 한다. 그러지 않으면 processor 메소드에서 해당 작업을 하기 위해 만들어 놓은 메소드를 찾질 못한다(chunk 메소드에 타입을 명시하지 않으면 <Object, Object>로 인식하기 때문이다
                .reader(shoppingCartFlatFileItemReader)
                .processor(customItemProcessor)
                // .writer(customShoppingCartJpaItemWriter)
                .writer(jpaItemWriter)
                .faultTolerant()
                .skipLimit(10)
                .skip(BatchException.class)
                .skip(FlatFileParseException.class)
                .listener(customSkipListener)
                .listener(chunkListenerImpl)
                // .writer(shoppingCartDebugItemWriter())
                // .writer(shoppingCartLoggingItemWriter())
                .build();

        // reader에서 발생할수 있는 예외
        // IOException, JsonParseException, JsonMappingException
    }

    /*
    reader와 processor 의 호출 관계에 대한 내용을 정리하고자 한다
    https://jojoldu.tistory.com/331 에 가보면 reader 와 processor 간의 호출은 chunk size 만큼 반복되는 것으로 설명되어 있다
    예를 들어 내가 chunksize를 10 으로 지정했으면
    reader.read 메소드 -> processor.process 메소드 호출이 10번 일어나야 하는데
    실제 중단점을 걸고 호출 관계를 보면
    reader.read 메소드 10번 호출 -> processor.process 메소드 10번 호출
    이런식으로 전개되고 있다
    그래서 skip 과 관련된 exception 발생시 관련 log를 기록하기 위해 현재 처리중인 행과 라인을 보관하는 bean을 만들어서 작업하고 있었으나..
    이렇게 chunk 단위로 read 메소드 반복호출 -> process 메소드 반복호출 이렇게 전개가 되어버리면
    현재 처리중인 행을 보관할 것이 아니라..
    예외가 발생했을때 예외가 발생한 행을 보관하는 컨셉으로 진행해야 할 듯 하다
    이것과 관련해서는 2019년 11월 26일 위에 언급한 글에 이와 관련된 내용으로 질문 댓글을 달은 상황이다
    이러다 보니 skiplistner의 skip 관련 메소드 호출 시점이
    line을 처리하는 과정에서 예외가 발생하여 skip 메소드를 호출하는 것이 아니라
    chunk 단위로 skip 메소드가 한번만 호출되는 듯 하다..이 부분은 좀더 검증이 필요하다다     */
    @Bean
    @StepScope
    public FlatFileItemReader<ShoppingCartVO> shoppingCartFlatFileItemReader(@Value("#{jobParameters[jobDateTime]}") String jobDateTime, ObjectMapper objectMapper) {
        // String todayString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // String todayString2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String todayString = jobDateTime.substring(0, 4) + "-" + jobDateTime.substring(4, 6) + "-" + jobDateTime.substring(6, 8);
        String filePathName = filePath + "/" + fileNamePrefix + todayString + "." + fileExt;

        // LineMapper<LineResultVO> lineResultVOLineMapper = new LineResultVOLineMapper(objectMapper, 15, -1);
        LineMapper<ShoppingCartVO> ShoppingCartVOLineMapper = new ShoppingCartVOLineMapper(objectMapper, 15, -1);

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
    public CustomItemProcessor customItemProcessor(@Value("#{jobParameters[jobDateTime]}") String jobDateTime) {
        return new CustomItemProcessor(userRepository, productRepository);
    }

    @Bean
    @StepScope
    public JpaItemWriter<ShoppingCart> shoppingCartJpaItemWriter(@Value("#{jobParameters[jobDateTime]}") String jobDateTime) {
        JpaItemWriter<ShoppingCart> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    /*
    @Bean
    @StepScope
    public CustomShoppingCartJpaItemWriter customShoppingCartJpaItemWriter(@Value("#{jobParameters[jobDateTime]}") String jobDateTime, DataShareBean dataShareBean) {
        CustomShoppingCartJpaItemWriter customShoppingCartJpaItemWriter = new CustomShoppingCartJpaItemWriter();
        customShoppingCartJpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return customShoppingCartJpaItemWriter;
    }
    */

    @Bean
    @StepScope
    public LoggingItemWriter<ShoppingCart> shoppingCartLoggingItemWriter(@Value("#{jobParameters[jobDateTime]}") String jobDateTime) {
        return new LoggingItemWriter<>("ShoppingCart : {}");
    }

    @Bean
    @StepScope
    public CustomSkipListener customSkipListener(@Value("#{jobParameters[jobDateTime]}") String jobDateTime) {
        return CustomSkipListener.builder().jobDateTime(jobDateTime).build();
    }

    @Bean
    @StepScope
    public ChunkListenerImpl chunkListenerImpl() {
        return ChunkListenerImpl.builder().build();
    }

    private ItemWriter<ShoppingCart> shoppingCartDebugItemWriter() {
        return list -> {
            for(ShoppingCart shoppingCart : list) {
                logger.info("ShoppingCart : {}", shoppingCart);
            }
        };
    }
}
