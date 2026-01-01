package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OsType - 운영체제 타입 분류
 *
 * <p>User-Agent 문자열에서 파싱된 운영체제 타입을 나타냅니다. 검색/필터링/관리 목적으로 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum OsType {
    WINDOWS("Windows", Pattern.compile("Windows NT ([\\d.]+)")),
    MACOS("macOS", Pattern.compile("Mac OS X ([\\d_]+)")),
    LINUX("Linux", null),
    IOS("iOS", Pattern.compile("iPhone OS ([\\d_]+)|CPU OS ([\\d_]+)")),
    ANDROID("Android", Pattern.compile("Android ([\\d.]+)")),
    CHROME_OS("Chrome OS", Pattern.compile("CrOS [^)]+"));

    private final String displayName;
    private final Pattern versionPattern;

    OsType(String displayName, Pattern versionPattern) {
        this.displayName = displayName;
        this.versionPattern = versionPattern;
    }

    /**
     * 화면 표시용 이름을 반환합니다.
     *
     * @return 화면 표시용 OS명
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * User-Agent 문자열에서 OS 버전을 추출합니다.
     *
     * @param userAgentString User-Agent 문자열
     * @return OS 버전 (추출 불가 시 null)
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
            if (version != null) {
                return version.replace("_", ".");
            }
        }
        return null;
    }

    /**
     * User-Agent 문자열에서 OS 타입을 파싱합니다.
     *
     * @param userAgentString User-Agent 문자열
     * @return 파싱된 OsType (식별 불가 시 LINUX)
     */
    public static OsType parseFrom(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return LINUX;
        }

        String ua = userAgentString.toLowerCase();

        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return IOS;
        }
        if (ua.contains("android")) {
            return ANDROID;
        }
        if (ua.contains("windows")) {
            return WINDOWS;
        }
        if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return MACOS;
        }
        if (ua.contains("cros")) {
            return CHROME_OS;
        }
        if (ua.contains("linux")) {
            return LINUX;
        }

        return LINUX;
    }
}
