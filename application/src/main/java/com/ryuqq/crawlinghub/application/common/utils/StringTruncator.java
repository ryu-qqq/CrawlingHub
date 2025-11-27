package com.ryuqq.crawlinghub.application.common.utils;

/**
 * 문자열 truncate 유틸리티
 *
 * <p>로깅, 에러 메시지 등에서 긴 문자열을 잘라서 출력할 때 사용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class StringTruncator {

    private static final int DEFAULT_MAX_LENGTH = 200;
    private static final String ELLIPSIS = "...";

    private StringTruncator() {
        // utility class
    }

    /**
     * 문자열을 기본 길이(200)로 truncate
     *
     * @param str 대상 문자열
     * @return truncate된 문자열
     */
    public static String truncate(String str) {
        return truncate(str, DEFAULT_MAX_LENGTH);
    }

    /**
     * 문자열을 지정된 길이로 truncate
     *
     * @param str 대상 문자열
     * @param maxLength 최대 길이
     * @return truncate된 문자열
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + ELLIPSIS;
    }
}
