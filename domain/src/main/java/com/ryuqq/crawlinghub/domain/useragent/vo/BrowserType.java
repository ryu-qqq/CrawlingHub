package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BrowserType - 브라우저 타입 분류
 *
 * <p>User-Agent 문자열에서 파싱된 브라우저 타입을 나타냅니다. 검색/필터링/관리 목적으로 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum BrowserType {
    CHROME("Chrome", Pattern.compile("Chrome/([\\d.]+)")),
    SAFARI("Safari", Pattern.compile("Version/([\\d.]+).*Safari")),
    FIREFOX("Firefox", Pattern.compile("Firefox/([\\d.]+)")),
    EDGE("Edge", Pattern.compile("Edg/([\\d.]+)|Edge/([\\d.]+)")),
    OPERA("Opera", Pattern.compile("OPR/([\\d.]+)|Opera/([\\d.]+)")),
    SAMSUNG_BROWSER("Samsung Browser", Pattern.compile("SamsungBrowser/([\\d.]+)"));

    private final String displayName;
    private final Pattern versionPattern;

    BrowserType(String displayName, Pattern versionPattern) {
        this.displayName = displayName;
        this.versionPattern = versionPattern;
    }

    /**
     * 화면 표시용 이름을 반환합니다.
     *
     * @return 화면 표시용 브라우저명
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * User-Agent 문자열에서 브라우저 버전을 추출합니다.
     *
     * @param userAgentString User-Agent 문자열
     * @return 브라우저 버전 (추출 불가 시 null)
     */
    public String extractVersion(String userAgentString) {
        if (versionPattern == null || userAgentString == null) {
            return null;
        }

        Matcher matcher = versionPattern.matcher(userAgentString);
        if (matcher.find()) {
            String version = matcher.group(1);
            if (version == null && matcher.groupCount() > 1) {
                version = matcher.group(2);
            }
            return version;
        }
        return null;
    }

    /**
     * User-Agent 문자열에서 브라우저 타입을 파싱합니다.
     *
     * <p>파싱 우선순위: Edge > Opera > Samsung > Firefox > Chrome > Safari (Chrome/Safari는 다른 브라우저 UA에도
     * 포함되어 있으므로 후순위)
     *
     * @param userAgentString User-Agent 문자열
     * @return 파싱된 BrowserType (식별 불가 시 CHROME)
     */
    public static BrowserType parseFrom(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return CHROME;
        }

        // Edge 체크 (Edg/ 또는 Edge/)
        if (userAgentString.contains("Edg/") || userAgentString.contains("Edge/")) {
            return EDGE;
        }

        // Opera 체크 (OPR/ 또는 Opera/)
        if (userAgentString.contains("OPR/") || userAgentString.contains("Opera/")) {
            return OPERA;
        }

        // Samsung Browser 체크
        if (userAgentString.contains("SamsungBrowser/")) {
            return SAMSUNG_BROWSER;
        }

        // Firefox 체크
        if (userAgentString.contains("Firefox/")) {
            return FIREFOX;
        }

        // Chrome 체크 (Safari도 Chrome UA에 포함되어 있음)
        if (userAgentString.contains("Chrome/")) {
            return CHROME;
        }

        // Safari 체크 (iOS Safari는 Chrome이 없음)
        if (userAgentString.contains("Safari/") && userAgentString.contains("Version/")) {
            return SAFARI;
        }

        return CHROME;
    }
}
