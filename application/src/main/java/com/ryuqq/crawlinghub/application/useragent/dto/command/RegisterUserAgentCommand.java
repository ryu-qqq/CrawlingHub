package com.ryuqq.crawlinghub.application.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;

/**
 * RegisterUserAgentCommand - UserAgent 등록 Command DTO
 *
 * <p>UserAgent 등록에 필요한 정보를 전달하는 Command 객체입니다. User-Agent 문자열과 함께 파싱된 메타데이터를 포함합니다.
 *
 * @param userAgentString User-Agent 문자열
 * @param deviceType 디바이스 타입 (MOBILE, TABLET, DESKTOP)
 * @param deviceBrand 디바이스 브랜드
 * @param osType OS 타입
 * @param osVersion OS 버전 (nullable)
 * @param browserType 브라우저 타입
 * @param browserVersion 브라우저 버전 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterUserAgentCommand(
        String userAgentString,
        DeviceType deviceType,
        DeviceBrand deviceBrand,
        OsType osType,
        String osVersion,
        BrowserType browserType,
        String browserVersion) {

    /**
     * 필수 필드만으로 Command를 생성합니다. 메타데이터는 User-Agent 문자열에서 자동 파싱됩니다.
     *
     * @param userAgentString User-Agent 문자열
     * @param deviceType 디바이스 타입
     * @return 생성된 RegisterUserAgentCommand
     */
    public static RegisterUserAgentCommand withAutoParsing(
            String userAgentString, DeviceType deviceType) {
        DeviceBrand deviceBrand = DeviceBrand.parseFrom(userAgentString);
        OsType osType = OsType.parseFrom(userAgentString);
        String osVersion = osType.extractVersion(userAgentString);
        BrowserType browserType = BrowserType.parseFrom(userAgentString);
        String browserVersion = browserType.extractVersion(userAgentString);

        return new RegisterUserAgentCommand(
                userAgentString,
                deviceType,
                deviceBrand,
                osType,
                osVersion,
                browserType,
                browserVersion);
    }
}
