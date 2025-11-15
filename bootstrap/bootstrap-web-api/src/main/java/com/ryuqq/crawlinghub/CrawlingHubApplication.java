package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CrawlingHub Web API 애플리케이션 진입점
 *
 * <p>AWS 기반 분산 크롤링 시스템의 메인 Web API 서버입니다.
 * Hexagonal Architecture(Ports & Adapters) 패턴을 적용하여
 * 도메인 로직과 인프라 계층을 분리합니다.</p>
 *
 * <h2>아키텍처 구조</h2>
 * <ul>
 *   <li>Domain Layer: 비즈니스 로직 및 도메인 모델</li>
 *   <li>Application Layer: Use Case 및 비즈니스 흐름 조율</li>
 *   <li>Adapter-In (REST): REST Controller, DTO, Exception Handler</li>
 *   <li>Adapter-Out: JPA, Redis, EventBridge, HTTP Client 등</li>
 * </ul>
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>크롤링 사이트 관리 (등록, 조회, 수정, 삭제)</li>
 *   <li>크롤링 워크플로우 설정</li>
 *   <li>크롤링 스케줄 관리 (EventBridge 통합)</li>
 *   <li>크롤링 실행 모니터링 및 결과 조회</li>
 * </ul>
 *
 * <p><strong>Scheduler와의 분리:</strong></p>
 * <ul>
 *   <li>Web API는 @EnableScheduling 없음 (Auto Scaling 가능)</li>
 *   <li>@Scheduled 메서드는 bootstrap-scheduler에서 실행 (ECS Desired Count: 1 고정)</li>
 *   <li>Fast Path: EventListener (@Async, 즉시 처리)</li>
 *   <li>Fallback Path: @Scheduled (실패 시 재처리) ← bootstrap-scheduler</li>
 * </ul>
 *
 * <p><strong>배포 전략:</strong></p>
 * <ul>
 *   <li>Web API: Auto Scaling 활성화 (2-10 인스턴스, CPU/Memory 기반)</li>
 *   <li>Scheduler: 별도 배포 (Desired Count: 1, 스케줄러 중복 실행 방지)</li>
 *   <li>SQS Listener: 별도 배포 (SQS 처리량 기반 Auto Scaling)</li>
 * </ul>
 *
 * @author CrawlingHub Platform Team (platform@crawlinghub.com)
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ryuqq.crawlinghub.adapter.out.persistence")
@EntityScan(basePackages = "com.ryuqq.crawlinghub.adapter.out.persistence")
public class CrawlingHubApplication {

    /**
     * 애플리케이션 진입점
     *
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(CrawlingHubApplication.class, args);
    }
}
