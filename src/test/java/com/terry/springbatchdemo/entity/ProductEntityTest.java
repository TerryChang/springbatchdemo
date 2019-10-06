package com.terry.springbatchdemo.entity;

import com.terry.springbatchdemo.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2")
@DataJpaTest
public class ProductEntityTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    ProductRepository productRepository;

    private Long productIdx;

    @Before
    public void before() {
        Product product = productRepository.save(Product.builder().productName("Oracle 11g XE").productPrice(10000).build());

        productIdx = product.getIdx();
        // ShoppingCart shoppingCart = shoppingCartRepository.save(ShoppingCart.builder().user(user).shoppingItemList(new ArrayList<ShoppingItem>()).build());
        testEntityManager.flush();
        testEntityManager.clear();
        logger.info("before method product : {}", product.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        Optional<Product> optionalProduct = productRepository.findProductByProductName("Oracle 11g XE");
        assertThat(optionalProduct.isPresent(), is(true));
        Product product = optionalProduct.get();
        assertThat(product.getProductName(), is("Oracle 11g XE"));
        assertThat(product.getProductPrice(), is(10000));
        logger.info("제대로 생성되었는지 테스트의 Product 객체 : {}", product.toString());
    }

    @Test
    public void 상품이름과_가격_수정_테스트() {
        logger.info("상품이름과_가격_수정_테스트 시작");
        Optional<Product> optionalProduct = productRepository.findById(productIdx);
        assertThat(optionalProduct.isPresent(), is(true));
        Product product = optionalProduct.orElse(null);
        product.update("Oracle 12c", 200000);

        /**
         * EntityManager 객체의 flush 메소드를 실행시켜 DB에 이를 반영한뒤 clear 메소드를 실행시켜 영속성 컨텍스트를 초기화 시켜버리면
         * 기존의 영속성 컨텍스트에 존재하던 엔티티 객체들이 준영속 상태로 바뀌게 된다.
         * 그런 상태에서 준영속 상태의 엔티티 객체와 동등한 엔티티를 조회하게 되면 영속된 상태의 새로운 객체를 생성한뒤 이를 영속성 컨텍스트에 넣어두기 때문에
         * 기존의 준영속 상태의 객체와는 또다른 객체가 생성이 된다
         * 이런 과정을 거치기 때문에 엔티티 객체의 값을 수정한뒤 EntityManager 객체의 flush, clear 메소드를 실행시킨후 다시 같은 값의 엔티티 객체를 조회하게 되면
         * 수정된 내용이 반영되어 있는 DB에서 엔티티 객체를 생성해서 넣어두게 되기 때문에 DB에 반영되어 있는지를 확인할 수 있다
         */
        testEntityManager.flush();
        testEntityManager.clear();

        optionalProduct = productRepository.findProductByProductName("Oracle 12c");
        assertThat(optionalProduct.isPresent(), is(true));
        Product updateProduct = optionalProduct.orElse(null);

        // 준영속 상태의 객체와 새로 조회해서 영속상태로 만든 객체가 같은 인스턴스가 아닌것을 테스트하기 위한 코드
        // 같은 인스턴스가 아니란 것은 DB에서 조회해서 엔티티 객체를 생성했음을 의미한다.
        assertThat(product, not(sameInstance(updateProduct)));
        assertThat(updateProduct.getProductName(), is("Oracle 12c"));
        assertThat(updateProduct.getProductPrice(), is(200000));
        logger.info("상품이름과_가격_수정_테스트 끝");
    }

    @Test
    public void 삭제테스트() {
        Optional<Product> optionalProduct = productRepository.findById(productIdx);
        assertThat(optionalProduct.isPresent(), is(true));
        Product product = optionalProduct.orElse(null);
        productRepository.deleteById(productIdx);

        testEntityManager.flush();
        testEntityManager.clear();

        optionalProduct = productRepository.findById(productIdx);
        assertThat(optionalProduct.isPresent(), is(false));
    }
}
