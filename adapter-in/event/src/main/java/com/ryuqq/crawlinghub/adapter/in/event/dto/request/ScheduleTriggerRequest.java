package com.ryuqq.crawlinghub.adapter.in.event.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * EventBridge에서 SQS로 전달되는 Schedule 트리거 요청.
 * <p>
 * EventBridge는 {@code {"sellerId": 1}} 형태의 JSON 메시지를 전송합니다.
 * SQS FIFO Queue를 통해 셀러별 중복 처리를 방지합니다.
 * </p>
 *
 * @param sellerId 트리거할 셀러 ID (필수)
 *
 * @author Sang-won Ryu
 * @since 1.0
 */
public record ScheduleTriggerRequest(
    @JsonProperty("sellerId")
    Long sellerId
) {

    /**
     * Compact Constructor로 입력 검증.
     * <p>
     * sellerId는 필수 값이며 null이 될 수 없습니다.
     * </p>
     *
     * @throws IllegalArgumentException sellerId가 null인 경우
     */
    public ScheduleTriggerRequest {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 필수 값입니다");
        }
        if (sellerId <= 0) {
            throw new IllegalArgumentException("sellerId는 양수여야 합니다");
        }
    }
}
