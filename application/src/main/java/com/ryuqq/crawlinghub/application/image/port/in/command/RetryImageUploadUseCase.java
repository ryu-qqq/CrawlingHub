package com.ryuqq.crawlinghub.application.image.port.in.command;

import com.ryuqq.crawlinghub.application.image.dto.response.ImageUploadRetryResponse;

/**
 * 이미지 업로드 재시도 UseCase
 *
 * <p>실패한 이미지 업로드 요청을 배치로 재시도합니다.
 *
 * <p><strong>재시도 대상</strong>:
 *
 * <ul>
 *   <li>FAILED 상태이면서 최대 재시도 횟수 미만인 Outbox
 *   <li>PROCESSING 상태이면서 타임아웃된 Outbox
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RetryImageUploadUseCase {

    /**
     * 재시도 가능한 이미지 업로드 배치 처리
     *
     * @return 처리 결과 (처리 건수, 성공/실패, 추가 데이터 유무)
     */
    ImageUploadRetryResponse execute();
}
