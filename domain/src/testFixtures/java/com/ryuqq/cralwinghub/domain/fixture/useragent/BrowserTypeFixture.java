package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;

/**
 * BrowserType Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class BrowserTypeFixture {

    /**
     * 기본 브라우저 타입 (CHROME)
     *
     * @return BrowserType
     */
    public static BrowserType aDefaultBrowserType() {
        return BrowserType.CHROME;
    }

    /**
     * Chrome 브라우저 타입
     *
     * @return BrowserType
     */
    public static BrowserType aChromeBrowserType() {
        return BrowserType.CHROME;
    }

    /**
     * Safari 브라우저 타입
     *
     * @return BrowserType
     */
    public static BrowserType aSafariBrowserType() {
        return BrowserType.SAFARI;
    }

    /**
     * Firefox 브라우저 타입
     *
     * @return BrowserType
     */
    public static BrowserType aFirefoxBrowserType() {
        return BrowserType.FIREFOX;
    }

    /**
     * Edge 브라우저 타입
     *
     * @return BrowserType
     */
    public static BrowserType anEdgeBrowserType() {
        return BrowserType.EDGE;
    }

    /**
     * Samsung Browser 타입
     *
     * @return BrowserType
     */
    public static BrowserType aSamsungBrowserType() {
        return BrowserType.SAMSUNG_BROWSER;
    }

    /**
     * Opera 브라우저 타입
     *
     * @return BrowserType
     */
    public static BrowserType anOperaBrowserType() {
        return BrowserType.OPERA;
    }

    private BrowserTypeFixture() {
        // Utility class
    }
}
