package com.terry.springbatchdemo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class LoggingItemWriter<T> implements ItemWriter<T> {
    private final String startMessage;

    @Override
    public void write(List<? extends T> items) throws Exception {
        for(T item : items) {
            if("".equals(startMessage))
                logger.info(item.toString());
            else
                logger.info(startMessage, item.toString());
        }
    }
}
