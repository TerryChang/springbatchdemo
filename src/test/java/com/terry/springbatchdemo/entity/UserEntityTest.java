package com.terry.springbatchdemo.entity;

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

import javax.swing.text.html.Option;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2")
@DataJpaTest
public class UserEntityTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    Long userIdx;

    @Before
    public void before() {
        User user = userRepository.save(User.builder().name("오라클").loginId("oracle.com").build());
        userIdx = user.getIdx();
        testEntityManager.flush();
        testEntityManager.clear();
        logger.info("before method user : {}", user.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        User user = userRepository.findByLoginId("oracle.com");
        assertThat(user.getName(), is("오라클"));
        assertThat(user.getLoginId(), is("oracle.com"));
        logger.info("제대로 생성되었는지 테스트의 User 객체 : {}", user.toString());
    }

    @Test
    public void 로그인_아이디와_이름수정_테스트() {
        logger.info("로그인_아이디와_이름수정_테스트 시작");
        Optional<User> userOptional = userRepository.findById(userIdx);
        assertThat(userOptional.isPresent(), is(true));
        User user = userOptional.get();
        user.update("수정된 이름", "modifyLoginId");

        testEntityManager.flush();
        testEntityManager.clear();

        userOptional = userRepository.findById(userIdx);
        assertThat(userOptional.isPresent(), is(true));
        User updateUser = userOptional.get();

        assertThat(user, (sameInstance(updateUser)));
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
}
