package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.Arrays;

/**
 * Device Type Value Object
 *
 * <p>User-Agent 문자열로부터 파싱된 디바이스 타입을 관리합니다.
 *
 * <p><strong>디바이스 타입</strong>:
 *
 * <ul>
 *   <li>MOBILE: 스마트폰 (iPhone, Android Phone)
 *   <li>TABLET: 태블릿 (iPad, Android Tablet)
 *   <li>DESKTOP: 데스크톱/노트북 (Windows, macOS, Linux)
 * </ul>
 *
 * <p><strong>파싱 규칙</strong>:
 *
 * <ul>
 *   <li>iPhone, Android (Mobile 키워드) → MOBILE
 *   <li>iPad, Android Tablet → TABLET
 *   <li>Macintosh, Windows, Linux → DESKTOP
 *   <li>파싱 실패 시 → DESKTOP (기본값)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record DeviceType(Type type) {

    /** 디바이스 타입 Enum */
    public enum Type {
        MOBILE("Mobile"),
        TABLET("Tablet"),
        DESKTOP("Desktop");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * User-Agent 문자열로부터 디바이스 타입 파싱
     *
     * <p><strong>파싱 우선순위</strong>:
     *
     * <ol>
     *   <li>TABLET: iPad, Android.*Tablet
     *   <li>MOBILE: iPhone, Android, Mobile
     *   <li>DESKTOP: Macintosh, Windows, Linux (기본값)
     * </ol>
     *
     * @param userAgentString User-Agent 헤더 문자열
     * @return DeviceType
     */
    public static DeviceType parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new DeviceType(Type.DESKTOP);
        }

        String ua = userAgentString.toLowerCase();
        boolean isAndroid = ua.contains("android");
        boolean hasMobile = ua.contains("mobile");

        // 1. TABLET 체크 (iPad, Android Tablet)
        if (ua.contains("ipad") || (isAndroid && ua.contains("tablet"))) {
            return new DeviceType(Type.TABLET);
        }

        // 2. MOBILE 체크 (iPhone, Mobile 키워드 포함)
        if (ua.contains("iphone") || hasMobile) {
            return new DeviceType(Type.MOBILE);
        }

        // 3. DESKTOP (기본값)
        return new DeviceType(Type.DESKTOP);
    }

    /**
     * 타입 이름으로 생성
     *
     * @param typeName 타입 이름 (MOBILE, TABLET, DESKTOP)
     * @return DeviceType
     */
    public static DeviceType of(String typeName) {
        Type parsedType =
                Arrays.stream(Type.values())
                        .filter(t -> t.name().equalsIgnoreCase(typeName))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "유효하지 않은 DeviceType: " + typeName));
        return new DeviceType(parsedType);
    }

    /**
     * Type Enum으로 생성
     *
     * @param type Type Enum
     * @return DeviceType
     */
    public static DeviceType of(Type type) {
        return new DeviceType(type);
    }

    /**
     * 모바일 디바이스인지 확인
     *
     * @return MOBILE이면 true
     */
    public boolean isMobile() {
        return this.type == Type.MOBILE;
    }

    /**
     * 태블릿 디바이스인지 확인
     *
     * @return TABLET이면 true
     */
    public boolean isTablet() {
        return this.type == Type.TABLET;
    }

    /**
     * 데스크톱 디바이스인지 확인
     *
     * @return DESKTOP이면 true
     */
    public boolean isDesktop() {
        return this.type == Type.DESKTOP;
    }

    /**
     * 타입 이름 반환
     *
     * @return 타입 이름 (MOBILE, TABLET, DESKTOP)
     */
    public String getTypeName() {
        return this.type.name();
    }

    /**
     * 표시 이름 반환
     *
     * @return 표시 이름 (Mobile, Tablet, Desktop)
     */
    public String getDisplayName() {
        return this.type.getDisplayName();
    }
}
