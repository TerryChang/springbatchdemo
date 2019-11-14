package com.terry.springbatchdemo.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class DataShareBean {
    private LineInfo currentLineinfo;
    private List<LineInfo> writerLineInfoList = new ArrayList<>();

    public void addWriterLineInfoList(LineInfo lineInfo) {
        this.writerLineInfoList.add(lineInfo);
    }

    public void removeWriterLineInfoList(LineInfo lineInfo) {
        if(this.writerLineInfoList.contains(lineInfo))
            this.writerLineInfoList.remove(lineInfo);
    }

    public void clearWriterLineInfoList() {
        this.writerLineInfoList.clear();
    }

    public int getWriterLineInfoListSize() {
        return this.writerLineInfoList.size();
    }
}
