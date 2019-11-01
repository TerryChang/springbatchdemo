package com.terry.springbatchdemo.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.terry.springbatchdemo.ShoppingCartVOLineMapper;
import com.terry.springbatchdemo.config.json.ProductVODeserializer;
import com.terry.springbatchdemo.config.json.ShoppingCartVODeserializer;
import com.terry.springbatchdemo.config.json.ShoppingItemVODeserializer;
import com.terry.springbatchdemo.entity.Product;
import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.entity.ShoppingItem;
import com.terry.springbatchdemo.entity.User;
import com.terry.springbatchdemo.repository.ProductRepository;
import com.terry.springbatchdemo.repository.ShoppingCartRepository;
import com.terry.springbatchdemo.repository.ShoppingItemRepository;
import com.terry.springbatchdemo.repository.UserRepository;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import com.terry.springbatchdemo.vo.ShoppingItemVO;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                .<ShoppingCartVO, ShoppingCart>chunk(10)// chunk 메소드에 반드시 작업할 타입을 명시해주어야 한다. 그러지 않으면 processor 메소드에서 해당 작업을 하기 위해 만들어 놓은 메소드를 찾질 못한다(chunk 메소드에 타입을 명시하지 않으면 <Object, Object>로 인식하기 때문이다
                .reader(shoppingCartFlatFileItemReader())
                .processor(shoppingCartItemProcessor())
                .writer(shoppingCartJpaItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<ShoppingCartVO> shoppingCartFlatFileItemReader() {
        String todayString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String todayString2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String filePathName = filePath + "/" + fileNamePrefix + todayString + "." + fileExt;
        LineMapper<ShoppingCartVO> shoppingCartVOLineMapper = new ShoppingCartVOLineMapper(new ObjectMapper(), 15, -1);

        return new FlatFileItemReaderBuilder<ShoppingCartVO>()
                .lineMapper(shoppingCartVOLineMapper)
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
    public ItemProcessor<ShoppingCartVO, ShoppingCart> shoppingCartItemProcessor() throws BatchException {
        /*
        return new ItemProcessor<ShoppingCart, ShoppingCart>() {
            @Override
            public ShoppingCart process(ShoppingCart item) throws Exception {
                return item;
            }
        };
        */
        return  item -> {
            ShoppingCart shoppingCartEntity = null;
            Set<ShoppingItem> shoppingItemEntitySet = new LinkedHashSet<>();
            User userEntity = userRepository.findByLoginId(item.getLoginId());
            if(userEntity == null) {
                // 사용자가 없다는 예외 던지는 부분
                throw new BatchException("ShoppingItem - Not Exists User : " + item.getLoginId());
            }

            List<ShoppingItemVO> shoppingItemVOList = item.getShoppingItemList();
            for(ShoppingItemVO shoppingItem : shoppingItemVOList) {
                ProductVO productVO = shoppingItem.getProduct();
                long productPrice = productVO.getProductPrice();
                int cnt = shoppingItem.getCnt();
                long totalPriceByProduct = shoppingItem.getTotalPriceByProduct();

                if(productPrice * cnt != totalPriceByProduct) {
                    // 상품단가 * 갯수 != 상품별 총 가격일 경우 맞지 않다고 예외를 던지는 부분
                    throw new BatchException("ShoppingItem - Mismatch product price * cnt and totalPrice");
                }

                Optional<Product> optionalProduct = productRepository.findById(new Long(productVO.getProductId()));
                Product productEntity = null;
                if(optionalProduct.isPresent()) {
                    productEntity = optionalProduct.get();
                    if(productEntity.getProductPrice() != productPrice) {
                        // 상품 단가가 등록되어 있는 상품단가와 틀리다는 예외를 던지는 부분
                        throw new BatchException("ShoppingItem - Mismatch already database product price and log product price");
                    }
                    if(productEntity.getProductPrice() * cnt != totalPriceByProduct) {
                        // 등록된 상품 단가 * 갯수와 등록하고자 하는 총액이 틀리다는 예외를 던지는 부분
                        throw new BatchException("ShoppingItem - Mismatch database product price * cnt and log total price");
                    }
                    ShoppingItem shoppingItemEntity = new ShoppingItem(productEntity, cnt);
                    shoppingItemEntity.setShoppingCart(shoppingCartEntity); // 일단은 null로 되어 있는 shoppingCartEntity 변수를 설정하고 맨 마지막에 이 변수를 초기화 시킨다
                    shoppingItemEntitySet.add(shoppingItemEntity);
                } else {
                    // 상품이 없다는 예외 던지는 부분
                }
            }

            shoppingCartEntity = new ShoppingCart(userEntity, shoppingItemEntitySet);
            return shoppingCartEntity;
        };
        // return null;
    }

    @Bean
    public JpaItemWriter<ShoppingCart> shoppingCartJpaItemWriter() {
        JpaItemWriter<ShoppingCart> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
