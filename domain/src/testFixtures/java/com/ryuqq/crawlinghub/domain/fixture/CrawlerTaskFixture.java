package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.TaskId;

/**
 * 크롤러 작업 관련 테스트 데이터 생성 Fixture
 *
 * <p>크롤러 작업과 관련된 Value Object와 Enum의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultTaskId()} - 새로운 TaskId 생성</li>
 *   <li>{@link #defaultCrawlerTaskType()} - 기본 작업 타입 (MINISHOP)</li>
 *   <li>{@link #defaultCrawlerTaskStatus()} - 기본 작업 상태 (WAITING)</li>
 * </ul>
 */
public class CrawlerTaskFixture {

    /**
     * 기본 TaskId 생성
     *
     * @return 새로운 UUID 기반 TaskId
     */
    public static TaskId defaultTaskId() {
        return TaskId.generate();
    }

    /**
     * 기본 CrawlerTaskType 반환
     *
     * @return MINISHOP 타입
     */
    public static CrawlerTaskType defaultCrawlerTaskType() {
        return CrawlerTaskType.MINISHOP;
    }

    /**
     * 기본 CrawlerTaskStatus 반환
     *
     * @return WAITING 상태
     */
    public static CrawlerTaskStatus defaultCrawlerTaskStatus() {
        return CrawlerTaskStatus.WAITING;
    }
}
