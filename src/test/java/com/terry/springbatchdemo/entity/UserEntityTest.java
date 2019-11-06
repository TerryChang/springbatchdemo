package com.terry.springbatchdemo.entity;

import com.terry.springbatchdemo.repository.ShoppingCartRepository;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2_log4jdbc")          // log4jdbc가 적용된 H2 DataSource를 사용하도록 profile 설정
@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class UserEntityTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShoppingCartRepository shoppingCartRepository;

    private Long userIdx;

    @Before
    public void before() {
        User user = userRepository.save(User.builder().name("페이스북").loginId("facebook.com").build());
        userIdx = user.getIdx();
        testEntityManager.flush();
        testEntityManager.clear();
        logger.info("before method user : {}", user.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        Optional<User> optionalUser = userRepository.findByLoginId("oracle.com");
        assertThat(optionalUser.isPresent(), is(true));
        User user = optionalUser.get();
        assertThat(user.getName(), is("오라클"));
        assertThat(user.getLoginId(), is("oracle.com"));
        logger.info("제대로 생성되었는지 테스트의 User 객체 : {}", user.toString());
    }

    @Test
    public void 로그인_아이디와_이름수정_테스트() {
        logger.info("로그인_아이디와_이름수정_테스트 시작");
        Optional<User> optionalUser = userRepository.findById(userIdx);
        assertThat(optionalUser.isPresent(), is(true));
        User user = optionalUser.get();
        user.update("수정된 이름", "modifyLoginId");

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

        optionalUser = userRepository.findById(userIdx);
        assertThat(optionalUser.isPresent(), is(true));
        User updateUser = optionalUser.get();

        // 준영속 상태의 객체와 새로 조회해서 영속상태로 만든 객체가 같은 인스턴스가 아닌것을 테스트하기 위한 코드
        // 같은 인스턴스가 아니란 것은 DB에서 조회해서 엔티티 객체를 생성했음을 의미한다.
        assertThat(user, not(sameInstance(updateUser)));
        assertThat(user.getName(), is("수정된 이름"));
        assertThat(user.getLoginId(), is("modifyLoginId"));
        logger.info("로그인_아이디와_이름수정_테스트 끝");
    }

    @Test
    public void EntityManager를_Flush한뒤_엔티티객체가_영속성컨텍스트에_영속된상태로_존재하는지_테스트() {
        User user = userRepository.save(User.builder().name("오라클1").loginId("oracle1.com").build());
        userRepository.save(user);
        Long idx = user.getIdx();
        testEntityManager.flush();
        Optional<User> optionalUser = userRepository.findById(idx);
        assertThat(optionalUser.isPresent(), is(true));
        User selectUser = optionalUser.get();
        assertThat(user, sameInstance(selectUser));
    }

    @Test
    public void 삭제테스트() {
        Optional<User> optionalUser = userRepository.findById(userIdx);
        assertThat(optionalUser.isPresent(), is(true));
        User user = optionalUser.get();
        userRepository.deleteById(userIdx);

        testEntityManager.flush();
        testEntityManager.clear();

        optionalUser = userRepository.findById(userIdx);
        assertThat(optionalUser.isPresent(), is(false));

    }

    /**
     * 이 테스트 코드를 만든 이유는 배치 코딩을 진행하면서 진행되어지는 SQL문을 보니
     * 같은 타입의 엔티티 객체를 여러개 저장할때 먼저 그 여러개 갯수 만큼 시퀀스를 돌려서 시퀀스를 구하고 그 다음에 insert 문이 실행되는것이 확인되었다.
     * 개인적인 생각은 select sequence -> insert -> select sequence -> insert 이렇게 흘러가는줄 알았는데
     * 그게 아니라 select sequence -> select sequence -> insert -> insert 이렇게 실행이 되는 것이 확인되었기 때문이다.
     * 그래서 이를 확인하느라 아래와 같이 User Entity 2개를 등록하는 테스트 코드를 만들어서 여기에서 찍히는 SQL 로그를 보고 이를 확인하려 했던것이다.
     * 아마 추측엔 flush 하는 과정에서 내가 insert 해야 할 객체가 영속성 컨텍스트에 이미 있기 때문에 먼저 해당 sequence들만 돌려서 엔티티 객체에 설정하는 작업을 모두 다 마친뒤
     * 그 다음에 객체들을 DB에 넣거나 수정하는 작업을 하는것이 아닐까 하는 생각을 하게 된다
     */
    @Test
    public void 사용자_두명_등록_테스트() {

        User twitterUser = userRepository.save(User.builder().name("트위터").loginId("twitter.com").build());
        User kakaoUser = userRepository.save(User.builder().name("카카오").loginId("kakao.com").build());
        List<User> userList = new ArrayList<>();
        userList.add(twitterUser);
        userList.add(kakaoUser);
        userRepository.saveAll(userList);
        testEntityManager.flush();
        testEntityManager.clear();
        Optional<User> optionalTwitterUser = userRepository.findByLoginId("twitter.com");
        assertThat(optionalTwitterUser.isPresent(), is(true));
        User selectTwitterUser = optionalTwitterUser.get();
        assertThat(selectTwitterUser.getName(), is("트위터"));
        assertThat(selectTwitterUser.getLoginId(), is("twitter.com"));
        Optional<User> optionalKakaoUser = userRepository.findByLoginId("kakao.com");
        assertThat(optionalKakaoUser.isPresent(), is(true));
        User selectKakaoUser = optionalKakaoUser.get();
        assertThat(selectKakaoUser.getName(), is("카카오"));
        assertThat(selectKakaoUser.getLoginId(), is("kakao.com"));

    }
}
