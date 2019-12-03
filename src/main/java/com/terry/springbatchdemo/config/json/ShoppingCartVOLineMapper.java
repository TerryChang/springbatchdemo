package com.terry.springbatchdemo.config.json;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terry.springbatchdemo.config.BatchException;
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

    @Override
    public ShoppingCartVO mapLine(String line, int lineNumber) throws Exception {
        ShoppingCartVO shoppingCartVO = null;
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

        // 객체 매핑 과정에서 현재 어떤 라인을 매핑중인지 매핑 대상 라인의 줄번호와 줄 자체의 내용을 객체에 설정한다
        // 처리중인 라인에 대한 정보(줄번호, 줄 자체 내용 등)은 json 안에는 존재하지 않는 외부 정보이기 때문에
        // 이를 객체로 변환해서 넣을때 사용하기 위해 InjectableValues를 사용해서 넣는다
        // 해당 라인에 대한 정보를 설정하는 작업은 try-catch 모두에서 이루어지고 있는데 정상적으로 처리 되더라도 processor에서 해당 아이템에 대한 라인 정보를 읽어와야 하기 때문에
        // try 에서도 설정해주고 예외가 발생했을때도 예외가 발생한 해당 라인 정보를 설정해주고 있다
        // 단 예외가 발생했을 경우에는 객체 변환에는 실패했기 때문에 예외에다가 이 정보를 넘겨주기 위해서 LineInfo 객체를 생성한뒤 이를 BatchException 객체에 설정해주는 식으로 정보를 넘겨주고 있다
        try {
            /*
            InjectableValues.Std injectableValues = new InjectableValues.Std();
            injectableValues.addValue("lineNumber", lineNumber);
            injectableValues.addValue("lineContent", line);
            injectableValues.addValue("lineContentForJson", jobLine);
            objectMapper.setInjectableValues(injectableValues);
             */
            shoppingCartVO = objectMapper.readValue(jobLine, ShoppingCartVO.class);
            shoppingCartVO.setLineNumber(lineNumber);
            shoppingCartVO.setLineContent(line);
            shoppingCartVO.setLineContentForJson(jobLine);
        } catch (Exception e) {
            LineInfo lineInfo = new LineInfo();
            lineInfo.setLineNumber(lineNumber);
            lineInfo.setLineContent(line);
            lineInfo.setLineContentForJson(jobLine);
            BatchException batchException = new BatchException(lineInfo, e.getMessage(), e);
            throw batchException;
        }

        return  shoppingCartVO;
    }
}
