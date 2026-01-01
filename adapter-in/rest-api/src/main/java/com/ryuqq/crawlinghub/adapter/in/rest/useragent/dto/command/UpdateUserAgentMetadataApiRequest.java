package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * UpdateUserAgentMetadataApiRequest - UserAgent 메타데이터 수정 API 요청 DTO
 *
 * <p>UserAgent 메타데이터 수정 REST API 요청 바디를 나타냅니다. 모든 필드는 선택적이며, 전달된 필드만 수정됩니다.
 *
 * @param userAgentString User-Agent 문자열 (선택, 최대 500자)
 * @param deviceType 디바이스 타입 (선택: MOBILE, TABLET, DESKTOP)
 * @param deviceBrand 디바이스 브랜드 (선택: IPHONE, SAMSUNG, GENERIC 등)
 * @param osType OS 타입 (선택: WINDOWS, MACOS, IOS, ANDROID 등)
 * @param osVersion OS 버전 (선택, 최대 20자)
 * @param browserType 브라우저 타입 (선택: CHROME, SAFARI, FIREFOX 등)
 * @param browserVersion 브라우저 버전 (선택, 최대 30자)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 메타데이터 수정 요청")
public record UpdateUserAgentMetadataApiRequest(
        @Schema(
                        description = "User-Agent 문자열 (제공 시 메타데이터 자동 파싱)",
                        example =
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                        + " (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                @Size(max = 500, message = "User-Agent 문자열은 500자를 초과할 수 없습니다")
                String userAgentString,
        @Schema(description = "디바이스 타입", example = "DESKTOP") DeviceType deviceType,
        @Schema(description = "디바이스 브랜드", example = "GENERIC") DeviceBrand deviceBrand,
        @Schema(description = "OS 타입", example = "WINDOWS") OsType osType,
        @Schema(description = "OS 버전", example = "10.0")
                @Size(max = 20, message = "OS 버전은 20자를 초과할 수 없습니다")
                String osVersion,
        @Schema(description = "브라우저 타입", example = "CHROME") BrowserType browserType,
        @Schema(description = "브라우저 버전", example = "120.0.0.0")
                @Size(max = 30, message = "브라우저 버전은 30자를 초과할 수 없습니다")
                String browserVersion) {}
