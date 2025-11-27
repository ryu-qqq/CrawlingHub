package com.ryuqq.crawlinghub.application.task.dto.messaging;

/**
 * EventBridge 트리거 SQS 메시지 페이로드
 *
 * <p><strong>용도</strong>: EventBridge에서 SQS로 전송되는 스케줄러 트리거 메시지의 페이로드 구조
 *
 * <p><strong>트리거 흐름</strong>:
 *
 * <ol>
 *   <li>EventBridge Rule (cron 표현식) → 트리거 발생
 *   <li>EventBridge → SQS로 메시지 전송
 *   <li>SQS Listener가 메시지 수신
 *   <li>CrawlTask 생성 및 크롤링 실행
 * </ol>
 *
 * @param schedulerId Scheduler ID
 * @param sellerId Seller ID
 * @param schedulerName 스케줄러 이름
 * @param triggerTime 트리거 발생 시간 (ISO-8601)
 * @author development-team
 * @since 1.0.0
 */
public record EventBridgeTriggerPayload(
        Long schedulerId, Long sellerId, String schedulerName, String triggerTime) {}
