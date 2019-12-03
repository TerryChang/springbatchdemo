package com.terry.springbatchdemo.batch;

import com.terry.springbatchdemo.repository.ShoppingCartRepository;
import com.terry.springbatchdemo.repository.ShoppingItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles({"h2_log4jdbc", "local", "notebook"})          // log4jdbc가 적용된 H2 DataSource를 사용하도록 profile 설정
@SpringBatchTest
@SpringBootTest
/*
@Transactional
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
 */
// @ContextConfiguration(classes = {BatchJobConfig.class, TestBatchConfig.class})
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class ReadFileWriteDatabaseBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Test
    public void 배치작업_테스트() throws Exception {
        // String jobDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String jobDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        JobParameters jobParameters = new JobParametersBuilder().addString("jobDateTime", jobDateTime).toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        // 배치작업이 정상적으로 실행 완료 되었는지 체크하기 위한 코드
        assertThat(jobExecution.getStatus(), is(BatchStatus.COMPLETED));
        // 배치 작업을 마친뒤의 Database 부분에 대해 체크하기 위한 코드를 넣어야 한다
        // 이것은 현재 예측이 어려워서 일단은 패스
        // assertThat()

    }
}
