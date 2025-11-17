package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.execution.CrawlingScheduleExecution;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * CrawlingScheduleExecution 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlingScheduleExecution Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p><strong>Factory Method 패턴:</strong></p>
 * <ul>
 *   <li>{@link #pendingExecution()} - PENDING 상태의 기본 실행 인스턴스</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingScheduleExecutionFixture {

    private static final SellerId DEFAULT_SELLER_ID = new SellerId("seller_12345");

    /**
     * PENDING 상태의 CrawlingScheduleExecution 생성
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_12345</li>
     *   <li>Status: PENDING</li>
     *   <li>TotalTasksCreated: 0</li>
     *   <li>CompletedTasks: 0</li>
     *   <li>FailedTasks: 0</li>
     * </ul>
     *
     * @return PENDING 상태의 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution pendingExecution() {
        ScheduleId scheduleId = ScheduleId.generate();
        return CrawlingScheduleExecution.create(scheduleId, DEFAULT_SELLER_ID);
    }
}
