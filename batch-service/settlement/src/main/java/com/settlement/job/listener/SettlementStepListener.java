package com.settlement.job.listener;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SettlementStepListener implements StepExecutionListener, ChunkListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [{}] 스텝 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(">>> [{}] 스텝 완료 - Read: {}, Write: {}, Commit: {}, Skip: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeChunk(ChunkContext context) {

    }

    @Override
    public void afterChunk(ChunkContext context) {
        StepExecution stepExecution = context.getStepContext().getStepExecution();
        log.info(">>> [{}] 청크 처리 중... (Read: {}, Write: {})",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount());
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        StepExecution stepExecution = context.getStepContext().getStepExecution();
        log.error(">>> [{}] 청크 처리 중 에러 발생!", stepExecution.getStepName());
    }
}
