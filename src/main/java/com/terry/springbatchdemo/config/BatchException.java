package com.terry.springbatchdemo.config;

public class BatchException extends Exception {

    private final LineInfo lineIfo;

    public BatchException(LineInfo lineInfo, String message) {
        super(message);
        this.lineIfo = lineInfo;
    }

    public BatchException(LineInfo lineInfo, String message, Throwable t) {
        super(message, t);
        this.lineIfo = lineInfo;

    }
}
