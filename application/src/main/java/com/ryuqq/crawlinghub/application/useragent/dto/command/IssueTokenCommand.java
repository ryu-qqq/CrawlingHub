package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * 토큰 발급 Command
 *
 * @param userAgentId UserAgent ID (필수)
 * @param token 발급할 토큰 (필수)
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record IssueTokenCommand(
    Long userAgentId,
    String token
) {
    public IssueTokenCommand {
        if (userAgentId == null) {
            throw new IllegalArgumentException("userAgentId는 null일 수 없습니다");
        }
        if (userAgentId <= 0) {
            throw new IllegalArgumentException("userAgentId는 양수여야 합니다");
        }
        if (token == null) {
            throw new IllegalArgumentException("token은 null일 수 없습니다");
        }
        if (token.isBlank()) {
            throw new IllegalArgumentException("token은 비어있을 수 없습니다");
        }
    }
}



