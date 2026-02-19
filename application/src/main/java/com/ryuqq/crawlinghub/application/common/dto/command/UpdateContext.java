package com.ryuqq.crawlinghub.application.common.dto.command;

import java.time.Instant;

/**
 * 업데이트 작업 컨텍스트
 *
 * <p>Factory에서 생성하여 Service에 전달합니다.
 *
 * @param <ID> ID 타입
 * @param <UPDATE_DATA> 업데이트 데이터 타입
 * @param id 대상 ID
 * @param updateData 업데이트 데이터
 * @param changedAt 변경 시간
 */
public record UpdateContext<ID, UPDATE_DATA>(ID id, UPDATE_DATA updateData, Instant changedAt) {}
