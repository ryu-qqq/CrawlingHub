package com.ryuqq.crawlinghub.domain.crawl.task.vo;

import java.util.EnumSet;
import java.util.Set;

/**
 * CrawlTask 상태 Enum
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * WAITING → PUBLISHED → RUNNING → SUCCESS
 *                         ↓
 *                       FAILED → RETRY → PUBLISHED
 *                         ↓
 *                      TIMEOUT → RETRY → PUBLISHED
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlTaskStatus {

    /** 대기 중 - SQS 발행 전 */
    WAITING("대기 중"),

    /** 발행됨 - SQS에 메시지 발행 완료 */
    PUBLISHED("SQS 발행 완료"),

    /** 실행 중 - Consumer에서 처리 중 */
    RUNNING("실행 중"),

    /** 성공 - 크롤링 완료 */
    SUCCESS("성공"),

    /** 실패 - 크롤링 실패 */
    FAILED("실패"),

    /** 재시도 - 재시도 대기 */
    RETRY("재시도 대기"),

    /** 타임아웃 - 처리 시간 초과 */
    TIMEOUT("타임아웃");

    private static final Set<CrawlTaskStatus> IN_PROGRESS_STATUSES =
            EnumSet.of(WAITING, PUBLISHED, RUNNING);

    private static final Set<CrawlTaskStatus> TERMINAL_STATUSES =
            EnumSet.of(SUCCESS, FAILED);

    private final String description;

    CrawlTaskStatus(String description) {
        this.description = description;
    }

    /**
     * 상태 설명 반환
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 진행 중 상태 여부 확인
     *
     * @return WAITING, PUBLISHED, RUNNING 중 하나면 true
     */
    public boolean isInProgress() {
        return IN_PROGRESS_STATUSES.contains(this);
    }

    /**
     * 종료 상태 여부 확인
     *
     * @return SUCCESS 또는 FAILED면 true
     */
    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this);
    }
}
