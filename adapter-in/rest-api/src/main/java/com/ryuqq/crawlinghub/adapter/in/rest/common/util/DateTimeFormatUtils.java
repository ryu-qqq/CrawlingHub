package com.ryuqq.crawlinghub.adapter.in.rest.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * DateTimeFormatUtils - 날짜/시간 포맷 변환 유틸리티
 *
 * <p>REST API 응답에서 사용하는 날짜/시간 포맷 변환을 담당합니다.
 *
 * <p><strong>표준 포맷:</strong>
 *
 * <ul>
 *   <li>패턴: "yyyy-MM-dd HH:mm:ss"
 *   <li>예시: "2024-01-15 10:30:00"
 *   <li>TimeZone: Asia/Seoul (KST)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * Instant instant = Instant.now();
 * String formatted = DateTimeFormatUtils.format(instant);
 * // 결과: "2024-01-15 10:30:00"
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class DateTimeFormatUtils {

    /** 표준 날짜/시간 포맷 패턴 */
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 날짜/시간 포맷터 (Thread-safe) */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /** ISO 8601 포맷터 (API 응답용) */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** 타임존: Asia/Seoul (KST) */
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    /** 유틸리티 클래스 - 인스턴스화 금지 */
    private DateTimeFormatUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Instant를 표준 포맷 문자열로 변환
     *
     * <p>null 입력 시 null 반환 (null-safe)
     *
     * @param instant 변환할 Instant (nullable)
     * @return 포맷된 문자열 "yyyy-MM-dd HH:mm:ss" 또는 null
     */
    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZONE_ID);
        return localDateTime.format(FORMATTER);
    }

    /**
     * LocalDateTime을 표준 포맷 문자열로 변환
     *
     * <p>null 입력 시 null 반환 (null-safe)
     *
     * @param localDateTime 변환할 LocalDateTime (nullable)
     * @return 포맷된 문자열 "yyyy-MM-dd HH:mm:ss" 또는 null
     */
    public static String format(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(FORMATTER);
    }

    /**
     * Instant를 ISO 8601 포맷 문자열로 변환
     *
     * <p>API 응답에서 사용하는 국제 표준 형식입니다.
     *
     * <p>null 입력 시 null 반환 (null-safe)
     *
     * @param instant 변환할 Instant (nullable)
     * @return ISO 8601 포맷 문자열 "yyyy-MM-ddTHH:mm:ss" 또는 null
     */
    public static String formatIso8601(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZONE_ID);
        return localDateTime.format(ISO_FORMATTER);
    }

    /**
     * LocalDateTime을 ISO 8601 포맷 문자열로 변환
     *
     * <p>API 응답에서 사용하는 국제 표준 형식입니다.
     *
     * <p>null 입력 시 null 반환 (null-safe)
     *
     * @param localDateTime 변환할 LocalDateTime (nullable)
     * @return ISO 8601 포맷 문자열 "yyyy-MM-ddTHH:mm:ss" 또는 null
     */
    public static String formatIso8601(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(ISO_FORMATTER);
    }

    /**
     * 현재 시간을 ISO 8601 포맷 문자열로 반환
     *
     * <p>API 응답의 timestamp 필드에 사용합니다.
     *
     * @return 현재 시간의 ISO 8601 포맷 문자열
     */
    public static String nowIso8601() {
        return formatIso8601(Instant.now());
    }
}
