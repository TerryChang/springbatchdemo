package com.terry.springbatchdemo.config;

import lombok.*;

@Builder
@Getter
@EqualsAndHashCode
public class LineInfo {
    private final int lineNumber;
    private final String lineContent;
    private final String lineContentForJson;
}
