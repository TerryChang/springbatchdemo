package com.terry.springbatchdemo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;

@Getter
@Setter
public class LineInfo {

    @Transient
    private int lineNumber;

    @Transient
    private String lineContent;

    @Transient
    private String lineContentForJson;

    public LineInfo() {

    }
}
