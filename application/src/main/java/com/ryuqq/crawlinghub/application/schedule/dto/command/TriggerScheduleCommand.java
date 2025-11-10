package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 스케줄 트리거 Command (EventBridge에서 호출)
 * <p>
 * EventBridge는 {@code {"sellerId": 1}} 형태로 메시지를 전송하므로,
 * sellerId를 받아 해당 셀러의 활성 스케줄을 조회하여 실행합니다.
 * </p>
 *
 * @param sellerId 셀러 ID (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record TriggerScheduleCommand(
    Long sellerId
) {
    /**
     * Compact Constructor로 입력 검증.
     *
     * @throws IllegalArgumentException sellerId가 null이거나 양수가 아닌 경우
     */
    public TriggerScheduleCommand {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 필수 값입니다");
        }
        if (sellerId <= 0) {
            throw new IllegalArgumentException("sellerId는 양수여야 합니다");
        }
    }
}
