package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * UserAgent 비활성화 Command
 *
 * @param userAgentId UserAgent ID (필수)
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record DisableUserAgentCommand(
    Long userAgentId
) {
    public DisableUserAgentCommand {
        if (userAgentId == null) {
            throw new IllegalArgumentException("userAgentId는 null일 수 없습니다");
        }
        if (userAgentId <= 0) {
            throw new IllegalArgumentException("userAgentId는 양수여야 합니다");
        }
    }
}



