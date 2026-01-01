package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * RegisterUserAgentApiRequest - UserAgent 등록 API 요청 DTO
 *
 * <p>UserAgent 등록 REST API 요청 바디를 나타냅니다. User-Agent 문자열과 디바이스 타입을 필수로 받습니다.
 *
 * @param userAgentString User-Agent 문자열 (필수, 최대 500자)
 * @param deviceType 디바이스 타입 (필수: MOBILE, TABLET, DESKTOP)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterUserAgentApiRequest(
        @NotBlank(message = "User-Agent 문자열은 필수입니다")
                @Size(max = 500, message = "User-Agent 문자열은 500자를 초과할 수 없습니다")
                String userAgentString,
        @NotNull(message = "디바이스 타입은 필수입니다") DeviceType deviceType) {}
