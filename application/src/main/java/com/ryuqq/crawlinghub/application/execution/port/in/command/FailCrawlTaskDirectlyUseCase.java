package com.ryuqq.crawlinghub.application.execution.port.in.command;

/**
 * CrawlTask 즉시 실패 처리 UseCase (Port In)
 *
 * <p><strong>용도</strong>: SQS 리스너에서 RUNNING 전환 전에 발생한 영구적 오류(페이로드 변환 실패, 비즈니스 예외 등)를 처리하여
 * CrawlTask를 즉시 FAILED 상태로 전환합니다. PUBLISHED 상태의 고아 Task를 방지합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FailCrawlTaskDirectlyUseCase {

    /**
     * CrawlTask 즉시 실패 처리
     *
     * @param taskId CrawlTask ID
     * @param reason 실패 사유
     */
    void execute(Long taskId, String reason);
}
