package com.ryuqq.crawlinghub.application.sync.port.in.command;

import com.ryuqq.crawlinghub.application.sync.dto.messaging.ProductSyncPayload;

/**
 * SQS 메시지 기반 외부 서버 동기화 처리 UseCase
 *
 * <p><strong>용도</strong>: SQS Listener에서 수신한 ProductSync 메시지를 처리
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Outbox 조회 및 상태 검증 (이미 처리된 경우 skip)
 *   <li>PROCESSING 상태로 변경
 *   <li>ExternalProductServerClient로 동기화 요청
 *   <li>성공 시: COMPLETED 상태로 변경 + CrawledProduct synced 처리
 *   <li>실패 시: FAILED 상태로 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessProductSyncFromSqsUseCase {

    /**
     * SQS 메시지 기반 외부 서버 동기화 처리
     *
     * @param payload SQS 메시지 페이로드
     * @return 처리 결과 (true: 성공, false: skip 또는 실패)
     */
    boolean execute(ProductSyncPayload payload);
}
