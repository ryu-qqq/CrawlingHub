package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;

/**
 * OsType Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class OsTypeFixture {

    /**
     * 기본 OS 타입 (LINUX)
     *
     * @return OsType
     */
    public static OsType aDefaultOsType() {
        return OsType.LINUX;
    }

    /**
     * Windows OS 타입
     *
     * @return OsType
     */
    public static OsType aWindowsOsType() {
        return OsType.WINDOWS;
    }

    /**
     * macOS 타입
     *
     * @return OsType
     */
    public static OsType aMacOsType() {
        return OsType.MACOS;
    }

    /**
     * iOS 타입
     *
     * @return OsType
     */
    public static OsType anIosOsType() {
        return OsType.IOS;
    }

    /**
     * Android 타입
     *
     * @return OsType
     */
    public static OsType anAndroidOsType() {
        return OsType.ANDROID;
    }

    /**
     * Linux 타입
     *
     * @return OsType
     */
    public static OsType aLinuxOsType() {
        return OsType.LINUX;
    }

    /**
     * Chrome OS 타입
     *
     * @return OsType
     */
    public static OsType aChromeOsType() {
        return OsType.CHROME_OS;
    }

    private OsTypeFixture() {
        // Utility class
    }
}
