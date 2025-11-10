package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CrawlingHub SQS Listener 애플리케이션 진입점
 *
 * <p>AWS SQS 메시지 수신 전용 애플리케이션입니다.
 * EventBridge에서 SQS FIFO Queue로 전송된 Schedule Trigger 메시지를 수신하여 처리합니다.</p>
 *
 * <h2>아키텍처 구조</h2>
 * <ul>
 *   <li>Domain Layer: 비즈니스 로직 및 도메인 모델</li>
 *   <li>Application Layer: Use Case 및 비즈니스 흐름 조율</li>
 *   <li>Adapter-In (Event): SQS Listener (ScheduleTriggerSqsListener)</li>
 *   <li>Adapter-Out: JPA, Redis, EventBridge, HTTP Client 등</li>
 * </ul>
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>SQS FIFO Queue 메시지 수신 (ScheduleTriggerSqsListener)</li>
 *   <li>Schedule Trigger 처리 (TriggerScheduleUseCase)</li>
 *   <li>Message Group ID 기반 FIFO 보장 (셀러별 순서 보장)</li>
 *   <li>Visibility Timeout 30초 (처리 시간 고려)</li>
 * </ul>
 *
 * <p><strong>Web API와의 차이:</strong></p>
 * <ul>
 *   <li>Web API: REST Controller + Scheduling (@EnableScheduling)</li>
 *   <li>SQS Listener: SQS Listener만 (@SqsListener)</li>
 * </ul>
 *
 * <p><strong>배포 전략:</strong></p>
 * <ul>
 *   <li>Web API와 별도 배포 (독립적인 Auto Scaling)</li>
 *   <li>SQS 처리량에 따라 독립적으로 스케일 아웃</li>
 * </ul>
 *
 * @author CrawlingHub Platform Team (platform@crawlinghub.com)
 * @since 1.0.0
 */
@SpringBootApplication
public class SqsListenerApplication {

    /**
     * 애플리케이션 진입점
     *
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(SqsListenerApplication.class, args);
    }
}
