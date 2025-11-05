package com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.DomainException;
import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.exception.RateLimitExceededException;
import com.ryuqq.crawlinghub.domain.useragent.exception.TokenExpiredException;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentErrorCode;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentException;

import java.net.URI;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * UserAgent 도메인 예외를 HTTP 응답으로 변환하는 ErrorMapper
 *
 * <p>UserAgent Bounded Context의 모든 예외를 적절한 HTTP 상태 코드와 메시지로 매핑합니다.</p>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>NoAvailableUserAgentException → 404 Not Found</li>
 *   <li>InvalidUserAgentException → 400 Bad Request</li>
 *   <li>TokenExpiredException → 400 Bad Request</li>
 *   <li>RateLimitExceededException → 429 Too Many Requests</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith("USER_AGENT-");
    }

    @Override
    public ErrorMapper.MappedError map(DomainException ex, Locale locale) {
        if (!(ex instanceof UserAgentException userAgentException)) {
            throw new IllegalArgumentException("UserAgentException이 아닌 예외입니다: " + ex.getClass());
        }

        return switch (userAgentException) {
            case NoAvailableUserAgentException notFound -> new ErrorMapper.MappedError(
                HttpStatus.valueOf(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getHttpStatus()),
                UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getTitle(),
                notFound.message(),
                URI.create("/errors/user-agent-not-found")
            );

            case InvalidUserAgentException invalid -> new ErrorMapper.MappedError(
                HttpStatus.valueOf(UserAgentErrorCode.INVALID_USER_AGENT.getHttpStatus()),
                UserAgentErrorCode.INVALID_USER_AGENT.getTitle(),
                invalid.message(),
                URI.create("/errors/invalid-user-agent")
            );

            case TokenExpiredException expired -> new ErrorMapper.MappedError(
                HttpStatus.valueOf(UserAgentErrorCode.TOKEN_EXPIRED.getHttpStatus()),
                UserAgentErrorCode.TOKEN_EXPIRED.getTitle(),
                expired.message(),
                URI.create("/errors/token-expired")
            );

            case RateLimitExceededException rateLimit -> new ErrorMapper.MappedError(
                HttpStatus.valueOf(UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus()),
                UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getTitle(),
                rateLimit.message(),
                URI.create("/errors/rate-limit-exceeded")
            );

            default -> new ErrorMapper.MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unknown UserAgent Error",
                userAgentException.message(),
                URI.create("/errors/user-agent-unknown")
            );
        };
    }
}

