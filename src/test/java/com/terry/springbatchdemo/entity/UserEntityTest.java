package com.terry.springbatchdemo.entity;

import com.terry.springbatchdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("h2")
@DataJpaTest
public class UserEntityTest {

    @Autowired
    UserRepository userRepository;

    @Before
    public void before() {
        User user = userRepository.save(User.builder().name("오라클").loginId("oracle.com").build());
        logger.info("before method user : {}", user.toString());
    }

    @Test
    public void 제대로_생성되었는지_테스트() {
        User user = userRepository.findByLoginId("oracle.com");
        assertThat(user.getName(), is("오라클"));
        assertThat(user.getLoginId(), is("oracle.com"));
        logger.info("제대로 생성되었는지 테스트의 User 객체 : {}", user.toString());
    }
}
