package com.terry.springbatchdemo.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terry.springbatchdemo.config.DataShareBean;
import com.terry.springbatchdemo.config.LineInfo;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.item.file.LineMapper;

@RequiredArgsConstructor
@Setter
public class ShoppingCartVOLineMapper implements LineMapper<ShoppingCartVO> {

    private final ObjectMapper objectMapper;
    private final int startIdx;                 // 문자열을 잘라서 읽어야 할 경우 시작 index(잘라서 읽지 않게 할 경우 -1로 설정)
    private final int endIdx;                   // 문자열을 잘라서 읽어야 할 경우 종료 index(잘라서 읽지 않게 할 경우 -1로 설정)
    private final DataShareBean dataShareBean;

    @Override
    public ShoppingCartVO mapLine(String line, int lineNumber) throws Exception {
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
            LineInfo lineInfo = LineInfo.builder().lineNumber(lineNumber).lineContent(line).lineContentForJson(jobLine).build();
            // 객체 매핑전에 현재 어떤 라인을 매핑중인지 매핑 대상 라인의 줄번호와 줄 자체의 내용을 입력한다
            dataShareBean.setCurrentLineinfo(lineInfo);
            dataShareBean.addWriterLineInfoList(lineInfo);
            shoppingCartVO = objectMapper.readValue(jobLine, ShoppingCartVO.class);
        } catch (Exception e) {

        }

        return  shoppingCartVO;
    }
}
