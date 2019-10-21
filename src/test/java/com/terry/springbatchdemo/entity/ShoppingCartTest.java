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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// @DataJpaTest만 사용하면 내부적으로 자체 In Memory DataSource를 생성해버리기 때문에 테스트 과정에서 log4jdbc 같은 sql log 관련 라이브러리를 사용할 수 없다
// 어차피 H2 DB를 사용하기로 한것은 Test 용도일때만 사용하려 한 것이기 때문에 @AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE) 를 넣어서 테스트시 별도 DataSource를 생성하지 않게 하고
// @ActiveProfiles 를 h2로 설정 넣어줌으로써 log4jdbc를 사용하도록 유도해준다
@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2_log4jdbc")          // log4jdbc가 적용된 H2 DataSource를 사용하도록 profile 설정
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
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
    private Set<ShoppingItem> orgShoppingItemSet = null;
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

        Set<ShoppingItem> shoppingItemSet = new LinkedHashSet<>();
        shoppingItemSet.add(shoppingItem1);
        shoppingItemSet.add(shoppingItem2);
        shoppingItemSet.add(shoppingItem3);

        ShoppingCart shoppingCart = ShoppingCart.builder().user(user).shoppingItemSet(shoppingItemSet).build();

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
        orgShoppingItemSet = shoppingItemSet;
        orgShoppingCart = shoppingCart;

        testEntityManager.flush();
        testEntityManager.clear();
        logger.info("before method user : {}", user.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        logger.info("제대로_생성되었는지_테스트 시작");
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.findById(orgShoppingCart.getIdx());
        assertThat(optionalShoppingCart.isPresent(), is(true));
        ShoppingCart shoppingCart = optionalShoppingCart.orElse(null);
        assertThat(shoppingCart, is(orgShoppingCart));
        assertThat(shoppingCart.getUser(), is(orgUser));

        logger.info("--- shoppingItemSet 변수 값 넣기 전 ---");
        Set<ShoppingItem> shoppingItemsSet = shoppingCart.getShoppingItemSet();
        logger.info("--- shoppingItemSet 변수 값 넣은 후 ---");
        assertThat(shoppingItemsSet.size(), is(3));
        int listIdx = 0;

        // 정렬기준에 맞춰서 ShoppingItem 엔티티 객체들이 조회되었는지 체크
        Iterator<ShoppingItem> shoppingItemIterator = shoppingItemsSet.iterator();
        Iterator<ShoppingItem> orgShoppingItemIterator = orgShoppingItemSet.iterator();
        logger.info("--- loop 돌며 변수 값 체크 전 ---");
        for(int i=0; i < shoppingItemsSet.size(); i++) {
            ShoppingItem selectShoppingItem = shoppingItemIterator.next();
            ShoppingItem orgShoppingItem = orgShoppingItemIterator.next();

            logger.info("selectShoppingItem.getIdx() : {}, orgShoppingItem.getIdx() : {}", selectShoppingItem.getIdx(), orgShoppingItem.getIdx());
            assertThat(selectShoppingItem.getIdx().equals(orgShoppingItem.getIdx()), is(true));
        }
        logger.info("--- loop 돌며 변수 값 체크 후 ---");
        logger.info("제대로_생성되었는지_테스트 종료");
    }

    @Test
    public void user에대한_별도_select_구문이_실행되는_ShoppingCart_엔티티_조회_로그에서_Query체크() {
        logger.info("userInnerJoinFindById 메소드 실행전");
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.userInnerJoinFindById(orgShoppingCart.getIdx());
        logger.info("userInnerJoinFindById 메소드 실행후");
        assertThat(optionalShoppingCart.isPresent(), is(true));
        ShoppingCart shoppingCart = optionalShoppingCart.orElse(null);
        assertThat(shoppingCart, is(orgShoppingCart));
        assertThat(shoppingCart.getUser(), is(orgUser));

        logger.info("--- shoppingItemSet 변수 값 넣기 전 ---");
        Set<ShoppingItem> shoppingItemsSet = shoppingCart.getShoppingItemSet();
        logger.info("--- shoppingItemSet 변수 값 넣은 후 ---");
        assertThat(shoppingItemsSet.size(), is(3));
        assertThat(shoppingItemsSet.size() == orgShoppingItemSet.size(), is(true));

        // 정렬기준에 맞춰서 ShoppingItem 엔티티 객체들이 조회되었는지 체크
        Iterator<ShoppingItem> shoppingItemIterator = shoppingItemsSet.iterator();
        Iterator<ShoppingItem> orgShoppingItemIterator = orgShoppingItemSet.iterator();
        logger.info("--- loop 돌며 변수 값 체크 전 ---");
        for(int i=0; i < shoppingItemsSet.size(); i++) {
            ShoppingItem selectShoppingItem = shoppingItemIterator.next();
            ShoppingItem orgShoppingItem = orgShoppingItemIterator.next();

            logger.info("selectShoppingItem.getIdx() : {}, orgShoppingItem.getIdx() : {}", selectShoppingItem.getIdx(), orgShoppingItem.getIdx());
            assertThat(selectShoppingItem.getIdx().equals(orgShoppingItem.getIdx()), is(true));
        }
        logger.info("--- loop 돌며 변수 값 체크 후 ---");
    }

    /**
     * Set 인터페이스에서 item을 삭제한다고 해서 DB에서도 삭제가 되는게 아니다..
     * 관계만 제거되는 것이다.
     * 예를 들어 테이블상에서 foreign key를 null 로 setting 한 상태로 테이블에 놔두는 상황과 같다고 보면 된다
     * 그렇기때문에 직접적인 삭제 구문도 같이 실행시켜줘야 한다
     */
    @Test
    public void 일부_ShoppingItem_엔티티_객체_삭제후_업데이트_반영_테스트() {
        Optional<ShoppingCart> optionalShoppingCart = shoppingCartRepository.userInnerJoinFindById(orgShoppingCart.getIdx());
        ShoppingCart shoppingCart = optionalShoppingCart.orElse(null);
        Set<ShoppingItem> shoppingItemSet = shoppingCart.getShoppingItemSet();

        Iterator<ShoppingItem> shoppingItemIterator = shoppingItemSet.iterator();
        int idx = 0;
        Long deleteShoppingItemIdx = 0L;
        while(shoppingItemIterator.hasNext()) {
            if(idx == 1) {
                ShoppingItem shoppingItem = shoppingItemIterator.next();
                deleteShoppingItemIdx = shoppingItem.getIdx();
                shoppingCart.removeShoppingItem(shoppingItem);
                shoppingItemRepository.deleteById(shoppingItem.getIdx());
                break;
            }
            idx++;
        }

        logger.info("saveAndFlush 메소드 실행전");
        shoppingCartRepository.saveAndFlush(shoppingCart);
        logger.info("saveAndFlush 메소드 실행후");

        testEntityManager.flush();
        testEntityManager.clear();

        // 삭제된 뒤의 ShoppingCart 엔티티 객체 다시 조회
        optionalShoppingCart = shoppingCartRepository.userInnerJoinFindById(orgShoppingCart.getIdx());
        shoppingCart = optionalShoppingCart.orElse(null);
        shoppingItemSet = shoppingCart.getShoppingItemSet();
        assertThat(shoppingItemSet.size(), is(2)); // 삭제했기때문에 2가 나와야 한다

        shoppingItemIterator = shoppingItemSet.iterator();
        boolean notFind = true;
        while(shoppingItemIterator.hasNext()) {
            ShoppingItem shoppingItem = shoppingItemIterator.next();
            if(shoppingItem.getIdx() == deleteShoppingItemIdx) {
                notFind = false;
                break;
            }
        }

        assertThat(notFind, is(true));

    }

    @Test
    public void entityManager로_조회() {
        ShoppingCart shoppingCart = testEntityManager.find(ShoppingCart.class, orgShoppingCart.getIdx());
    }

    @Test
    public void LinkedHashSet의_equals_메소드가_저장된_순서도_같이_비교하여_판단하는지_테스트() {
        LinkedHashSet<Integer> lhs1 = new LinkedHashSet<>();
        LinkedHashSet<Integer> lhs2 = new LinkedHashSet<>();

        lhs1.add(10);
        lhs1.add(20);
        lhs1.add(30);
        lhs1.add(40);

        lhs2.add(10);
        lhs2.add(30);
        lhs2.add(40);
        lhs2.add(20);

        //LinkedHashSet의 equals 메소드가 순서도 같이 비교한다는 생각에 false라고 설정하고 테스트 했지만 false로 하면 테스트가 실패한다
        // 바꿔말하면 LinkedHashSet의 경우 크기와 들어가 있는 값에 대한 비교까지는 하지만 들어가 있는 순서는 비교하지 않는다는 뜻으로 해석된다
        assertThat(lhs1.equals(lhs2), is(false));
    }
}
