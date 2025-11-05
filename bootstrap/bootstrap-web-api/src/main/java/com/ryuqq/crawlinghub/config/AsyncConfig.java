package com.ryuqq.crawlinghub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async Configuration
 *
 * <p>@Async 어노테이션을 활성화합니다.</p>
 *
 * <p><strong>사용 위치:</strong></p>
 * <ul>
 *   <li>ScheduleEventListener: 트랜잭션 커밋 후 비동기로 Outbox Processor 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot 3.2+에서는 Virtual Threads 자동 사용 가능
    // application.yml에서 spring.threads.virtual.enabled=true 설정 시 자동 활성화
}

