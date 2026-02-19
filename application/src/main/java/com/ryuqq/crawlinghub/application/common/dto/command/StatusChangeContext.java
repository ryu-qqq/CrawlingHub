package com.ryuqq.crawlinghub.application.common.dto.command;

import java.time.Instant;

/**
 * 단순 상태 변경 컨텍스트
 *
 * <p>cancel, approve, reject, soft-delete 등 단순 상태 변경에 사용합니다.
 *
 * @param <ID> ID 타입
 * @param id 대상 ID
 * @param changedAt 변경 시간
 */
public record StatusChangeContext<ID>(ID id, Instant changedAt) {}
