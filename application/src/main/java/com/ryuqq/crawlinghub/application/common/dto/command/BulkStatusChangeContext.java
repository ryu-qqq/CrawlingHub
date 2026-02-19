package com.ryuqq.crawlinghub.application.common.dto.command;

import java.time.Instant;
import java.util.List;

/**
 * 일괄 상태 변경 컨텍스트
 *
 * @param <ID> ID 타입
 * @param ids 대상 ID 목록
 * @param changedAt 변경 시간
 */
public record BulkStatusChangeContext<ID>(List<ID> ids, Instant changedAt) {}
