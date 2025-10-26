package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 *   <li>Adapter Layer: 외부 시스템 연동 (REST API, JPA, AWS 등)</li>
 *   <li>Bootstrap Layer: 애플리케이션 실행 및 DI 컨테이너 설정</li>
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
 * @author CrawlingHub Platform Team
 * @since 1.0.0
 */
@SpringBootApplication
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
