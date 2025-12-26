package com.ryuqq.crawlinghub.application.image.port.in.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageOutboxTimeoutResult;

/**
 * 이미지 Outbox 타임아웃 처리 UseCase
 *
 * <p>PROCESSING 상태로 장시간 머물러 있는 Outbox를 FAILED로 변경하여 재시도 가능하게 합니다.
 *
 * <p><strong>타임아웃 조건</strong>:
 *
 * <ul>
 *   <li>PROCESSING 상태
 *   <li>processedAt 기준 타임아웃 시간 경과
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface HandleImageOutboxTimeoutUseCase {

    /**
     * 타임아웃된 ImageOutbox 처리
     *
     * @return 처리 결과
     */
    ImageOutboxTimeoutResult execute();
}
