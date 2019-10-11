package com.terry.springbatchdemo.entity;

import com.terry.springbatchdemo.repository.ProductRepository;
import com.terry.springbatchdemo.repository.ShoppingCartRepository;
import com.terry.springbatchdemo.repository.ShoppingItemRepository;
import com.terry.springbatchdemo.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2")
@DataJpaTest
public class ShoppingCartTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ShoppingItemRepository shoppingItemRepository;

    @Autowired
    ShoppingCartRepository shoppingCartRepository;

    /*
    private Long userIdx;
    private ArrayList<Long> productIdxList = new ArrayList<>();
    private ArrayList<Long> shoppingItemIdxList = new ArrayList<>();
    private Long shoppingCartIdx;
     */

    private User orgUser = null;
    private Product orgProduct1 = null;
    private Product orgProduct2 = null;
    private Product orgProduct3 = null;
    private List<ShoppingItem> orgShoppingItemList = null;
    private ShoppingCart orgShoppingCart = null;

    @Before
    public void before() {
        User user = userRepository.save(User.builder().name("오라클").loginId("oracle.com").build());
        Product product1 = productRepository.save(Product.builder().productName("oracle 11g xe").productPrice(200000).build());
        Product product2 = productRepository.save(Product.builder().productName("mysql").productPrice(150000).build());
        Product product3 = productRepository.save(Product.builder().productName("mariadb").productPrice(100000).build());

        ShoppingItem shoppingItem1 = ShoppingItem.builder().product(product1).cnt(5).build();
        ShoppingItem shoppingItem2 = ShoppingItem.builder().product(product2).cnt(3).build();
        ShoppingItem shoppingItem3 = ShoppingItem.builder().product(product3).cnt(2).build();

        List<ShoppingItem> shoppingItemList = new ArrayList<>();
        shoppingItemList.add(shoppingItem1);
        shoppingItemList.add(shoppingItem2);
        shoppingItemList.add(shoppingItem3);

        ShoppingCart shoppingCart = ShoppingCart.builder().user(user).shoppingItemList(shoppingItemList).build();

        shoppingItem1.setShoppingCart(shoppingCart);
        shoppingItem2.setShoppingCart(shoppingCart);
        shoppingItem3.setShoppingCart(shoppingCart);

        // ShoppingItem 엔티티 객체와 ShoppingCart 엔티티 객체의 저장순서는 ShoppingCart 엔티티 객체를 먼저 저장하고 ShoppingItem 객체를 그 담에 저장해야 한다
        // DB에서 저장할때 저장순서를 생각해보면 이해하기가 쉬운데...
        // ShoppingItem 엔티티 객체를 DB에 저장한다고 생각할 경우 저장하고자 하는 ShoppingItem 엔티티 객체와 연관을 맺고 있는 ShoppingCart 엔티티 객체의 key 값이 있어야 한다
        // ShoppingCart 엔티티 객체를 먼저 저장하고 ShoppingCart 엔티티 객체를 저장할 경우 jpa가 자동으로 ShoppingItem 엔티티 객체에 ShoppingCart 객체가 가지고 있는 key 값을 업데이트하겠지..요런 생각을 하면 안된다
        // 그래서 엔티티 객체를 저장할때 DB에 참조되는 key 값의 순서를 고려해서 거기에 맞춰서 저장해야 한다
        shoppingCartRepository.save(shoppingCart);

        shoppingItemRepository.save(shoppingItem1);
        shoppingItemRepository.save(shoppingItem2);
        shoppingItemRepository.save(shoppingItem3);

        orgUser = user;
        orgProduct1 = product1;
        orgProduct2 = product2;
        orgProduct3 = product3;
        orgShoppingItemList = shoppingItemList;
        orgShoppingCart = shoppingCart;

        testEntityManager.flush();
        testEntityManager.clear();
        logger.info("before method user : {}", user.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findById(orgShoppingCart.getIdx());
        assertThat(optionalShoppingCart.isPresent(), is(true));
        ShoppingCart shoppingCart = optionalShoppingCart.orElse(null);
        assertThat(shoppingCart, is(orgShoppingCart));
        assertThat(shoppingCart.getUser(), is(orgUser));

        logger.info("--- shoppingItemList 변수 값 넣기 전 ---");
        List<ShoppingItem> shoppingItemsList = shoppingCart.getShoppingItemList();
        logger.info("--- shoppingItemList 변수 값 넣은 후 ---");
        assertThat(shoppingItemsList.size(), is(3));
        int listIdx = 0;

        // 정렬기준에 맞춰서 ShoppingItem 엔티티 객체들이 조회되었는지 체크
        logger.info("--- loop 돌며 변수 값 체크 전 ---");
        for(ShoppingItem shoppingItem : shoppingItemsList) {
            try {
                assertThat(shoppingItem.getIdx(), is(orgShoppingItemList.get(listIdx++).getIdx()));
            } catch(Exception e) {
                logger.error("제대로_생성되었는지_테스트 메소드에서 ShoppingItemList 검사과정에서 예외 : 리스트 인덱스  - {}, {}", listIdx, e.getMessage());
                throw e;
            }
        }
        logger.info("--- loop 돌며 변수 값 체크 후 ---");
    }

    @Test
    public void fetch_join을_사용한_조회() {
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.fullJoinFindById(orgShoppingCart.getIdx());
        assertThat(optionalShoppingCart.isPresent(), is(true));
        ShoppingCart shoppingCart = optionalShoppingCart.orElse(null);
        assertThat(shoppingCart, is(orgShoppingCart));
        assertThat(shoppingCart.getUser(), is(orgUser));

        logger.info("--- shoppingItemList 변수 값 넣기 전 ---");
        List<ShoppingItem> shoppingItemsList = shoppingCart.getShoppingItemList();
        logger.info("--- shoppingItemList 변수 값 넣은 후 ---");
        assertThat(shoppingItemsList.size(), is(3));
        int listIdx = 0;

        // 정렬기준에 맞춰서 ShoppingItem 엔티티 객체들이 조회되었는지 체크
        logger.info("--- loop 돌며 변수 값 체크 전 ---");
        for(ShoppingItem shoppingItem : shoppingItemsList) {
            try {
                assertThat(shoppingItem.getIdx(), is(orgShoppingItemList.get(listIdx++).getIdx()));
            } catch(Exception e) {
                logger.error("제대로_생성되었는지_테스트 메소드에서 ShoppingItemList 검사과정에서 예외 : 리스트 인덱스  - {}, {}", listIdx, e.getMessage());
                throw e;
            }
        }
        logger.info("--- loop 돌며 변수 값 체크 후 ---");
    }
}
