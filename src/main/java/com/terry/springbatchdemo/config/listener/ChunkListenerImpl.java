package com.terry.springbatchdemo.config.listener;

import com.terry.springbatchdemo.config.DataShareBean;
import lombok.Builder;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

@Builder
public class ChunkListenerImpl implements ChunkListener {
    private final DataShareBean dataShareBean;

    @Override
    public void beforeChunk(ChunkContext context) {
        // 이전 chunk 단위 작업에서 보관중이었던 DataShareBean 클래스 객체 안의 writerLineInfoList 멤버변수 안의 내용물을 지운다
        // 이걸 지워야 새로운 chunk 단위에서 이를 보관할 수 있다
        // afterChunk 에서 하지 않은 이유는 chunk 단위로 처리하는 과정에서 예외가 발생할 경우 afterChunk 메소드를 실행하지 않을것 같아서(확인은 안됐음) 시작하기 전에 초기화하는 컨셉으로 잡았다
        dataShareBean.getWriterLineInfoList().clear();
    }

    @Override
    public void afterChunk(ChunkContext context) {

    }

    @Override
    public void afterChunkError(ChunkContext context) {

    }
}
