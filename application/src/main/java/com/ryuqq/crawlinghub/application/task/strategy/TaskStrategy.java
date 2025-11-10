package com.ryuqq.crawlinghub.application.task.strategy;

import com.ryuqq.crawlinghub.domain.task.Task;

/**
 * Task 처리 전략 인터페이스
 *
 * <p>각 TaskType별로 구체적인 처리 전략을 구현
 *
 * <p>구현체:
 * <ul>
 *   <li>{@link MetaTaskStrategy} - META 태스크 처리</li>
 *   <li>{@link MiniShopTaskStrategy} - MINI_SHOP 태스크 처리</li>
 *   <li>{@link ProductDetailTaskStrategy} - PRODUCT_DETAIL 태스크 처리</li>
 *   <li>{@link ProductOptionTaskStrategy} - PRODUCT_OPTION 태스크 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface TaskStrategy {

    /**
     * Task 실행
     *
     * <p>Template Method Pattern:
     * <ol>
     *   <li>Task 처리 (executeTask)</li>
     *   <li>Task 완료/실패 처리</li>
     *   <li>후속 Task 생성 및 Outbox 저장</li>
     * </ol>
     *
     * @param task 처리할 Task
     * @throws RuntimeException 처리 실패 시
     */
    void execute(Task task);
}
