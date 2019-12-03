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
import org.springframework.batch.item.file.FlatFileParseException;

import java.util.List;

@Builder
public class CustomSkipListener implements SkipListener<ShoppingCartVO, ShoppingCart> {

    private final Logger readExceptionLogger = LoggerFactory.getLogger("readExceptionLogger");
    private final Logger processExceptionLogger = LoggerFactory.getLogger("processExceptionLogger");
    private final Logger writeExceptionLogger = LoggerFactory.getLogger("writeExceptionLogger");

    private final String jobDateTime;

    @Override
    public void onSkipInRead(Throwable t) {

        // read 과정에서 예외가 발생하면 FlatFileParseException 으로 wrapping 되어서 던져지기 때문에 우리가 던진 BatchException을 꺼낼려면
        // getCause 메소드를 이용해서 Throwable 객체를 가져온뒤 이를 BatchException 으로 casting 을 해서 작업하도록 한다
        if(t instanceof  FlatFileParseException) {
            Throwable cause = t.getCause();

            String className = cause.getClass().getSimpleName();
            String exceptionMessage = cause.getMessage();

            if(cause instanceof BatchException) {
                BatchException batchException = (BatchException)cause;
                LineInfo lineInfo = batchException.getLineInfo();
                int lineNumber = lineInfo.getLineNumber();
                String lineContent = lineInfo.getLineContent();

                readExceptionLogger.info("Line Number {} : {} : {} - {}", lineNumber, className, exceptionMessage, lineContent);
            } else {
                readExceptionLogger.info("{} - {}", className, exceptionMessage);
            }

        } else { // FlatFileException 이 아닌 다른 예외가 던져질 경우 이에 대한 정보를 기록한다
            String className = t.getClass().getSimpleName();
            String exceptionMessage = t.getMessage();
            readExceptionLogger.info("{} - {}", className, exceptionMessage);
        }

    }

    @Override
    public void onSkipInProcess(ShoppingCartVO item, Throwable t) {
        int lineNumber = item.getLineNumber();
        String lineContent = item.getLineContent();
        String exceptionMessage = t.getMessage();

        String className = t.getClass().getSimpleName();
        processExceptionLogger.info("Line Number {} : {} : {} - {}", lineNumber, className, exceptionMessage, lineContent);
    }

    @Override
    public void onSkipInWrite(ShoppingCart item, Throwable t) {

        int lineNumber = item.getLineNumber();
        String lineContent = item.getLineContent();
        String exceptionMessage = t.getMessage();

        String className = t.getClass().getSimpleName();
        writeExceptionLogger.info("Line Number {} : {} : {} - {}", lineNumber, className, exceptionMessage, lineContent);
    }
}
