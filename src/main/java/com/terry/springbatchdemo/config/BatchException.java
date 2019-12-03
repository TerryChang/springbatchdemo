package com.terry.springbatchdemo.config;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BatchException extends Exception {

    private final LineInfo lineInfo;

    @Builder
    public BatchException(LineInfo lineInfo, String message) {
        super(message);
        this.lineInfo = lineInfo;
    }

    @Builder
    public BatchException(LineInfo lineInfo, String message, Throwable t) {
        super(message, t);
        this.lineInfo = lineInfo;
    }
}
