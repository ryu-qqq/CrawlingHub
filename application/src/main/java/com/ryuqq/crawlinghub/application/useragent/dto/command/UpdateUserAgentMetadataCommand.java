package com.ryuqq.crawlinghub.application.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;

/**
 * UpdateUserAgentMetadataCommand - UserAgent 메타데이터 수정 Command DTO
 *
 * <p>UserAgent 메타데이터 수정에 필요한 정보를 전달하는 Command 객체입니다. ID와 함께 수정할 메타데이터를 포함합니다.
 *
 * @param userAgentId 수정할 UserAgent ID
 * @param userAgentString User-Agent 문자열 (nullable - null이면 변경 안 함)
 * @param deviceType 디바이스 타입 (nullable - null이면 변경 안 함)
 * @param deviceBrand 디바이스 브랜드 (nullable - null이면 변경 안 함)
 * @param osType OS 타입 (nullable - null이면 변경 안 함)
 * @param osVersion OS 버전 (nullable)
 * @param browserType 브라우저 타입 (nullable - null이면 변경 안 함)
 * @param browserVersion 브라우저 버전 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateUserAgentMetadataCommand(
        Long userAgentId,
        String userAgentString,
        DeviceType deviceType,
        DeviceBrand deviceBrand,
        OsType osType,
        String osVersion,
        BrowserType browserType,
        String browserVersion) {

    /**
     * UserAgent 문자열만 변경하는 Command를 생성합니다. 메타데이터는 새 문자열에서 자동 파싱됩니다.
     *
     * @param userAgentId 수정할 UserAgent ID
     * @param userAgentString 새 User-Agent 문자열
     * @return 생성된 UpdateUserAgentMetadataCommand
     */
    public static UpdateUserAgentMetadataCommand withNewUserAgentString(
            Long userAgentId, String userAgentString) {
        DeviceBrand deviceBrand = DeviceBrand.parseFrom(userAgentString);
        OsType osType = OsType.parseFrom(userAgentString);
        String osVersion = osType.extractVersion(userAgentString);
        BrowserType browserType = BrowserType.parseFrom(userAgentString);
        String browserVersion = browserType.extractVersion(userAgentString);

        return new UpdateUserAgentMetadataCommand(
                userAgentId,
                userAgentString,
                null,
                deviceBrand,
                osType,
                osVersion,
                browserType,
                browserVersion);
    }
}
