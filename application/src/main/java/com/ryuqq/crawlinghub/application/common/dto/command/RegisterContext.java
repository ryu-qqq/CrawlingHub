package com.ryuqq.crawlinghub.application.common.dto.command;

import java.time.Instant;

/**
 * 등록 작업 컨텍스트
 *
 * <p>Factory에서 생성하여 Service에 전달합니다. TimeProvider는 Factory에서만 사용하고, Service는 이 컨텍스트의 changedAt()을
 * 재사용합니다.
 *
 * @param <T> 새 엔티티 타입
 * @param newEntity 새로 생성된 엔티티
 * @param changedAt 변경 시간
 */
public record RegisterContext<T>(T newEntity, Instant changedAt) {}
