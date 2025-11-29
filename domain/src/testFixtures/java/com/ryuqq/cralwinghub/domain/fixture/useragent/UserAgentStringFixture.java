package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;

/**
 * UserAgentString Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentStringFixture {

    // 실제 User-Agent 문자열 예시
    private static final String IPHONE_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML,"
                    + " like Gecko) Version/13.0 Mobile/15E148 Safari/604.1";

    private static final String IPAD_USER_AGENT =
            "Mozilla/5.0 (iPad; CPU OS 13_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)"
                    + " Version/13.0 Mobile/15E148 Safari/604.1";

    private static final String MACBOOK_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/120.0.0.0 Safari/537.36";

    private static final String ANDROID_MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/120.0.0.0 Mobile Safari/537.36";

    private static final String ANDROID_TABLET_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; SM-X900) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/120.0.0.0 Safari/537.36";

    private static final String WINDOWS_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/120.0.0.0 Safari/537.36";

    /**
     * 기본 User-Agent 문자열 (iPhone)
     *
     * @return UserAgentString
     */
    public static UserAgentString aDefaultUserAgentString() {
        return UserAgentString.of(IPHONE_USER_AGENT);
    }

    /**
     * iPhone User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString anIPhoneUserAgentString() {
        return UserAgentString.of(IPHONE_USER_AGENT);
    }

    /**
     * iPad User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString anIPadUserAgentString() {
        return UserAgentString.of(IPAD_USER_AGENT);
    }

    /**
     * MacBook User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString aMacBookUserAgentString() {
        return UserAgentString.of(MACBOOK_USER_AGENT);
    }

    /**
     * Android Mobile User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString anAndroidMobileUserAgentString() {
        return UserAgentString.of(ANDROID_MOBILE_USER_AGENT);
    }

    /**
     * Android Tablet User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString anAndroidTabletUserAgentString() {
        return UserAgentString.of(ANDROID_TABLET_USER_AGENT);
    }

    /**
     * Windows User-Agent 문자열
     *
     * @return UserAgentString
     */
    public static UserAgentString aWindowsUserAgentString() {
        return UserAgentString.of(WINDOWS_USER_AGENT);
    }

    /**
     * 특정 값으로 User-Agent 문자열 생성
     *
     * @param value User-Agent 문자열
     * @return UserAgentString
     */
    public static UserAgentString aUserAgentString(String value) {
        return UserAgentString.of(value);
    }

    private UserAgentStringFixture() {
        // Utility class
    }
}
