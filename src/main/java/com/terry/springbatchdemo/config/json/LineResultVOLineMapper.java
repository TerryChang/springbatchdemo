package com.terry.springbatchdemo.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terry.springbatchdemo.vo.LineResultVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.item.file.LineMapper;

@RequiredArgsConstructor
@Setter
public class LineResultVOLineMapper implements LineMapper<LineResultVO> {
    private final ObjectMapper objectMapper;
    private final int startIdx;            // 문자열을 잘라서 읽어야 할 경우 시작 index(잘라서 읽지 않게 할 경우 -1로 설정)
    private final int endIdx;              // 문자열을 잘라서 읽어야 할 경우 종료 index(잘라서 읽지 않게 할 경우 -1로 설정)

    @Override
    public LineResultVO mapLine(String line, int lineNumber) throws Exception {
        LineResultVO lineResultVO = null;
        ShoppingCartVO shoppingCartVO = null;

        try {
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
            shoppingCartVO = objectMapper.readValue(jobLine, ShoppingCartVO.class);
            lineResultVO = LineResultVO.builder().line(line).lineNumber(lineNumber).shoppingCartVO(shoppingCartVO).build();
        } catch(Exception e) {

        }
        return  lineResultVO;
    }
}
