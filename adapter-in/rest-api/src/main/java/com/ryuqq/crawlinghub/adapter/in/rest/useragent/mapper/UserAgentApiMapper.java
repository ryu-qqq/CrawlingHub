package com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.IssueTokenApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.UserAgentApiResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;

import org.springframework.stereotype.Component;

/**
 * UserAgentApiMapper - Application DTO ↔ REST API DTO 변환
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentApiMapper {

    /**
     * API Request → Application Command 변환
     *
     * @param userAgentId UserAgent ID
     * @param request API Request
     * @return Application Command
     */
    public IssueTokenCommand toCommand(Long userAgentId, IssueTokenApiRequest request) {
        if (request == null) {
            return null;
        }

        return new IssueTokenCommand(
                userAgentId,
                request.token()
        );
    }

    /**
     * Application Response → API Response 변환
     *
     * @param response Application Response
     * @return API Response
     */
    public UserAgentApiResponse toResponse(UserAgentResponse response) {
        if (response == null) {
            return null;
        }

        return new UserAgentApiResponse(
                response.userAgentId(),
                response.userAgentString(),
                response.tokenStatus().name(),
                response.remainingRequests(),
                response.tokenIssuedAt(),
                response.rateLimitResetAt(),
                response.createdAt(),
                response.updatedAt()
        );
    }
}

