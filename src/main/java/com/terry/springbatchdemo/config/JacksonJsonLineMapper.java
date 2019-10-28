package com.terry.springbatchdemo.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terry.springbatchdemo.entity.ShoppingCart;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.lang.reflect.Type;

@RequiredArgsConstructor
@Setter
public class JacksonJsonLineMapper<T> implements LineMapper<T>, InitializingBean {
    private final ObjectMapper objectMapper;
    private final TypeReference<T> typeReference;
    private final int startIdx;            // 문자열을 잘라서 읽어야 할 경우 시작 index(잘라서 읽지 않게 할 경우 -1로 설정)
    private final int endIdx;              // 문자열을 잘라서 읽어야 할 경우 종료 index(잘라서 읽지 않게 할 경우 -1로 설정)

    /*
    public JacksonJsonLineMapper(ObjectMapper objectMapper, TypeReference<T> typeReference, int startIdx, int endIdx) {
        // this.typeReference = new TypeReference<T>() {};
        this.typeReference = typeReference;
        this.objectMapper = objectMapper;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
     */

    @Override
    public T mapLine(String line, int lineNumber) throws Exception {
        String jobLine = null;
        if(startIdx == -1 && endIdx == -1) {
            jobLine = line;
        }else{
            if(startIdx != -1) {
                if(endIdx == -1) {
                    jobLine = line.substring(startIdx);
                }else {
                    jobLine = line.substring(startIdx, endIdx);
                }
            }
        }
        TypeReference<ShoppingCart> myTypeReference = new TypeReference<ShoppingCart>() {

        };
        return objectMapper.readValue(jobLine, typeReference);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(objectMapper, "The Jackson Object Mapper must be set");
    }
}
