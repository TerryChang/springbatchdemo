package com.terry.springbatchdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringbatchdemoApplication {

    public static void main(String[] args) {
        logger.info("Application Start in test1111");
        SpringApplication.run(SpringbatchdemoApplication.class, args);
    }

}
