package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * Leak된 UserAgent 복구 커맨드
 *
 * @param leakThresholdMillis BORROWED 상태 Leak 판정 기준 시간 (ms)
 * @author development-team
 * @since 1.0.0
 */
public record RecoverLeakedUserAgentCommand(long leakThresholdMillis) {

    public RecoverLeakedUserAgentCommand {
        if (leakThresholdMillis <= 0) {
            throw new IllegalArgumentException(
                    "leakThresholdMillis는 0보다 커야 합니다: " + leakThresholdMillis);
        }
    }

    public static RecoverLeakedUserAgentCommand of(long leakThresholdMillis) {
        return new RecoverLeakedUserAgentCommand(leakThresholdMillis);
    }
}
