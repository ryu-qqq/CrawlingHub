package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * DeviceBrand - 디바이스 브랜드 분류
 *
 * <p>User-Agent 문자열에서 파싱된 디바이스 브랜드를 나타냅니다. 검색/필터링/관리 목적으로 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum DeviceBrand {

    // Mobile
    IPHONE("iPhone"),
    SAMSUNG("Samsung"),
    PIXEL("Pixel"),
    XIAOMI("Xiaomi"),
    HUAWEI("Huawei"),
    OPPO("Oppo"),
    ONEPLUS("OnePlus"),

    // Tablet
    IPAD("iPad"),
    GALAXY_TAB("Galaxy Tab"),

    // Generic (Desktop or 식별 불가)
    GENERIC("Generic");

    private final String displayName;

    DeviceBrand(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 화면 표시용 이름을 반환합니다.
     *
     * @return 화면 표시용 브랜드명
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * User-Agent 문자열에서 디바이스 브랜드를 파싱합니다.
     *
     * @param userAgentString User-Agent 문자열
     * @return 파싱된 DeviceBrand (식별 불가 시 GENERIC)
     */
    public static DeviceBrand parseFrom(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return GENERIC;
        }

        String ua = userAgentString.toLowerCase();

        if (ua.contains("iphone")) {
            return IPHONE;
        }
        if (ua.contains("ipad")) {
            return IPAD;
        }
        if (ua.contains("pixel")) {
            return PIXEL;
        }
        if (ua.contains("sm-t") || ua.contains("galaxy tab")) {
            return GALAXY_TAB;
        }
        if (ua.contains("samsung") || ua.contains("sm-")) {
            return SAMSUNG;
        }
        if (ua.contains("xiaomi") || ua.contains("redmi") || ua.contains("poco")) {
            return XIAOMI;
        }
        if (ua.contains("huawei") || ua.contains("honor")) {
            return HUAWEI;
        }
        if (ua.contains("oppo") || ua.contains("realme")) {
            return OPPO;
        }
        if (ua.contains("oneplus")) {
            return ONEPLUS;
        }

        return GENERIC;
    }
}
