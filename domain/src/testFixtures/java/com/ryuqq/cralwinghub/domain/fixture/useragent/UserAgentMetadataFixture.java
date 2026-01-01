package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;

/**
 * UserAgentMetadata Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentMetadataFixture {

    /**
     * 기본 메타데이터 (GENERIC, LINUX, CHROME)
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata aDefaultMetadata() {
        return UserAgentMetadata.defaultMetadata();
    }

    /**
     * Windows Chrome 데스크톱 메타데이터
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata aWindowsChromeMetadata() {
        return UserAgentMetadata.of(
                DeviceBrand.GENERIC, OsType.WINDOWS, "10.0", BrowserType.CHROME, "120.0.0.0");
    }

    /**
     * macOS Safari 데스크톱 메타데이터
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata aMacSafariMetadata() {
        return UserAgentMetadata.of(
                DeviceBrand.GENERIC, OsType.MACOS, "14.0", BrowserType.SAFARI, "17.0");
    }

    /**
     * iPhone Safari 모바일 메타데이터
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata anIphoneSafariMetadata() {
        return UserAgentMetadata.of(
                DeviceBrand.IPHONE, OsType.IOS, "17.0", BrowserType.SAFARI, "17.0");
    }

    /**
     * Samsung Android Chrome 모바일 메타데이터
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata aSamsungChromeMetadata() {
        return UserAgentMetadata.of(
                DeviceBrand.SAMSUNG, OsType.ANDROID, "14", BrowserType.CHROME, "120.0.0.0");
    }

    /**
     * iPad Safari 태블릿 메타데이터
     *
     * @return UserAgentMetadata
     */
    public static UserAgentMetadata anIpadSafariMetadata() {
        return UserAgentMetadata.of(
                DeviceBrand.IPAD, OsType.IOS, "17.0", BrowserType.SAFARI, "17.0");
    }

    /**
     * User-Agent 문자열에서 파싱한 메타데이터
     *
     * @param userAgentString User-Agent 문자열
     * @return 파싱된 UserAgentMetadata
     */
    public static UserAgentMetadata fromUserAgentString(String userAgentString) {
        return UserAgentMetadata.parseFrom(userAgentString);
    }

    private UserAgentMetadataFixture() {
        // Utility class
    }
}
