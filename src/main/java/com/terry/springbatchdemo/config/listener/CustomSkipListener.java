package com.terry.springbatchdemo.config.listener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.terry.springbatchdemo.config.BatchException;
import com.terry.springbatchdemo.config.DataShareBean;
import com.terry.springbatchdemo.config.LineInfo;
import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

import java.util.List;

@Builder
public class CustomSkipListener implements SkipListener<ShoppingCartVO, ShoppingCart> {

    private final DataShareBean dataShareBean;

    private final Logger readExceptionLogger = LoggerFactory.getLogger("testBuyLogger");
    private final Logger processExceptionLogger = LoggerFactory.getLogger("testBuyLogger");
    private final Logger writeExceptionLogger = LoggerFactory.getLogger("testBuyLogger");

    private final String jobDateTime;

    @Override
    public void onSkipInRead(Throwable t) {

        // 현재 처리중인 행을 읽어서 log file에 기록한다
        int lineNumber = dataShareBean.getCurrentLineinfo().getLineNumber();
        String lineContent = dataShareBean.getCurrentLineinfo().getLineContent();
        String exceptionMessage = t.getMessage();
        if(t instanceof BatchException) {
            readExceptionLogger.info("Line Number {} BatchException : {}", lineNumber, exceptionMessage);
        } else if(t instanceof JsonParseException) {
            readExceptionLogger.info("Line Number {} JsonParseException : {}", lineNumber, exceptionMessage);
        } else if(t instanceof JsonMappingException) {
            readExceptionLogger.info("Line Number {} JsonMappingException : {}", lineNumber, exceptionMessage);
        } else {
            String className = t.getClass().getSimpleName();
            readExceptionLogger.info("Line Number {} " + className + " : {}", lineNumber, exceptionMessage);
        }

        readExceptionLogger.info("Line Content : {}", lineContent);
    }

    @Override
    public void onSkipInProcess(ShoppingCartVO item, Throwable t) {
        int lineNumber = dataShareBean.getCurrentLineinfo().getLineNumber();
        String lineContent = dataShareBean.getCurrentLineinfo().getLineContent();
        String exceptionMessage = t.getMessage();

        String className = t.getClass().getSimpleName();
        processExceptionLogger.info("Line Number {} " + className + " : {}", lineNumber, exceptionMessage);
        processExceptionLogger.info("Line Number {} " + className + " : {}", lineNumber, exceptionMessage);
    }

    @Override
    public void onSkipInWrite(ShoppingCart item, Throwable t) {
        // write의 경우에는 chunk 단위로 writer가 일어나기 때문에 예외가 발생할 경우 chunk 단위에 속한 모두가 write에 실패하게 된다
        // 그래서 write에 실패한 것에 대해서는 해당 item에 대한 부분이므로 그거는 그거대로 기록해주고
        // 해당 item을 포함한 chunk 단위로 실패한것은 그거대로 기록해주어야 한다

        String className = t.getClass().getSimpleName();
        writeExceptionLogger.info(className + " : {}", t.getMessage());
        writeExceptionLogger.info("Exception item : " + item.toString());
        List<LineInfo> writerLineInfoList = dataShareBean.getWriterLineInfoList();
        writeExceptionLogger.info("Chunk Item List Start");
        writerLineInfoList.forEach(lineInfo -> {
            writeExceptionLogger.info(item.toString());
        });
        writeExceptionLogger.info("Chunk Item List End");
    }
}
