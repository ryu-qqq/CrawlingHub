package com.ryuqq.crawlinghub.application.task.dto.messaging;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;

/**
 * CrawlTask SQS 메시지 페이로드
 *
 * <p><strong>용도</strong>: SQS를 통해 전송되는 CrawlTask 메시지의 페이로드 구조
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>adapter-out/aws-sqs: 메시지 발행 시 직렬화
 *   <li>adapter-in/sqs-listener: 메시지 수신 시 역직렬화
 * </ul>
 *
 * @param taskId Task ID
 * @param schedulerId Scheduler ID
 * @param sellerId Seller ID
 * @param taskType Task 유형 (PRODUCT, CATEGORY 등)
 * @param endpoint 크롤링 대상 URL
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskPayload(
        Long taskId, Long schedulerId, Long sellerId, String taskType, String endpoint) {

    /**
     * CrawlTask로부터 페이로드 생성
     *
     * @param crawlTask CrawlTask
     * @return CrawlTaskPayload
     */
    public static CrawlTaskPayload from(CrawlTask crawlTask) {
        return new CrawlTaskPayload(
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue(),
                crawlTask.getSellerIdValue(),
                crawlTask.getTaskType().name(),
                crawlTask.getEndpoint().toFullUrl());
    }
}
