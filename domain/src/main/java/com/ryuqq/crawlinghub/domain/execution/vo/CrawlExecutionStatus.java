package com.ryuqq.crawlinghub.domain.execution.vo;

import java.util.EnumSet;
import java.util.Set;

/**
 * CrawlExecution 상태 Enum
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * RUNNING → SUCCESS
 *    ↓
 * FAILED
 *    ↓
 * TIMEOUT
 * </pre>
 *
 * <p>CrawlTask와 달리 CrawlExecution은 재시도 없이 단일 실행의 결과만 기록합니다. 재시도 시에는 새로운 CrawlExecution이 생성됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlExecutionStatus {

    /** 실행 중 - 크롤링 진행 중 */
    RUNNING("실행 중"),

    /** 성공 - 크롤링 성공 */
    SUCCESS("성공"),

    /** 실패 - 크롤링 실패 (HTTP 에러, 파싱 에러 등) */
    FAILED("실패"),

    /** 타임아웃 - 실행 시간 초과 */
    TIMEOUT("타임아웃");

    private static final Set<CrawlExecutionStatus> TERMINAL_STATUSES =
            EnumSet.of(SUCCESS, FAILED, TIMEOUT);

    private static final Set<CrawlExecutionStatus> FAILURE_STATUSES = EnumSet.of(FAILED, TIMEOUT);

    private final String description;

    CrawlExecutionStatus(String description) {
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
     * 종료 상태 여부 확인
     *
     * @return SUCCESS, FAILED, TIMEOUT 중 하나면 true
     */
    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this);
    }

    /**
     * 실패 상태 여부 확인
     *
     * @return FAILED 또는 TIMEOUT이면 true
     */
    public boolean isFailure() {
        return FAILURE_STATUSES.contains(this);
    }

    /**
     * 성공 상태 여부 확인
     *
     * @return SUCCESS면 true
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
