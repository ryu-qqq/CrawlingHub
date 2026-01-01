package com.ryuqq.crawlinghub.application.image.port.in.command;

import com.ryuqq.crawlinghub.application.image.dto.messaging.ProductImagePayload;

/**
 * SQS 메시지 기반 이미지 업로드 처리 UseCase
 *
 * <p><strong>용도</strong>: SQS Listener에서 수신한 ProductImage 메시지를 처리
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Outbox 조회 및 상태 검증 (이미 처리된 경우 skip)
 *   <li>PROCESSING 상태로 변경
 *   <li>FileServerClient로 이미지 업로드 요청
 *   <li>성공 시: PROCESSING 상태 유지 (웹훅에서 COMPLETED 처리)
 *   <li>실패 시: FAILED 상태로 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessImageUploadFromSqsUseCase {

    /**
     * SQS 메시지 기반 이미지 업로드 처리
     *
     * @param payload SQS 메시지 페이로드
     * @return 처리 결과 (true: 성공, false: skip 또는 실패)
     */
    boolean execute(ProductImagePayload payload);
}
