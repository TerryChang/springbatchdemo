package com.terry.springbatchdemo.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineInfo {
    private int lineNumber;
    private String lineContent;
    private String lineContentForJson;

}
