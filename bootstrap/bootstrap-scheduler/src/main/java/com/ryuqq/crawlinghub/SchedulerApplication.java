package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CrawlingHub Scheduler 애플리케이션 진입점
 *
 * <p>@Scheduled 메서드 전용 애플리케이션입니다.
 * ECS Desired Count: 1로 고정하여 중복 실행을 방지합니다.</p>
 *
 * <h2>아키텍처 구조</h2>
 * <ul>
 *   <li>Domain Layer: 비즈니스 로직 및 도메인 모델</li>
 *   <li>Application Layer: Use Case 및 비즈니스 흐름 조율</li>
 *   <li>Scheduler: @Scheduled 메서드 실행 (Fallback Polling)</li>
 *   <li>Adapter-Out: JPA, Redis, EventBridge, HTTP Client 등</li>
 * </ul>
 *
 * <h2>실행되는 Scheduler</h2>
 * <ul>
 *   <li>ScheduleOutboxProcessor (1초마다) - Schedule Outbox Fallback</li>
 *   <li>ScheduleOutboxFinalizer (10분/1시간) - PENDING WAL 처리</li>
 *   <li>ProductSyncOutboxScheduler (5초마다) - Product Outbox Fallback</li>
 *   <li>TaskMessageOutboxScheduler - Task Message Outbox 처리</li>
 * </ul>
 *
 * <p><strong>중복 실행 방지 전략:</strong></p>
 * <ul>
 *   <li>ECS Task Definition: Desired Count = 1</li>
 *   <li>Auto Scaling 비활성화</li>
 *   <li>Health Check: /actuator/health</li>
 * </ul>
 *
 * <p><strong>Hybrid Pattern (Fast Path + Fallback Path):</strong></p>
 * <ul>
 *   <li>Fast Path: EventListener (@Async, 즉시 처리)</li>
 *   <li>Fallback Path: @Scheduled (실패 시 재처리) ← 이 Application</li>
 * </ul>
 *
 * <p><strong>배포 전략:</strong></p>
 * <ul>
 *   <li>Web API와 별도 배포 (독립적인 리소스 관리)</li>
 *   <li>CPU/Memory 낮음 (t3.micro 또는 Fargate 0.25vCPU)</li>
 *   <li>고가용성: ECS Service Auto Restart (Desired Count 1 유지)</li>
 * </ul>
 *
 * <p><strong>모니터링:</strong></p>
 * <ul>
 *   <li>CloudWatch Logs: Scheduler 실행 로그</li>
 *   <li>CloudWatch Metrics: Outbox 처리 성공/실패 건수</li>
 *   <li>Prometheus Metrics: /actuator/prometheus</li>
 * </ul>
 *
 * @author CrawlingHub Platform Team (platform@crawlinghub.com)
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SchedulerApplication {

    /**
     * 애플리케이션 진입점
     *
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
