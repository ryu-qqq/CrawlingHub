package com.ryuqq.crawlinghub.adapter.out.eventbridge.converter;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Cron 표현식 변환기
 * <p>
 * 간단한 표현식(HOURLY, DAILY, WEEKLY)을 AWS EventBridge Cron 형식으로 변환합니다.
 * </p>
 * <p>
 * AWS EventBridge Cron 형식: cron(분 시 일 월 요일 년도)
 * <ul>
 *   <li>UTC 기준</li>
 *   <li>년도 필드 필수</li>
 *   <li>예: cron(0 12 * * ? *) = 매일 UTC 12:00</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class CronExpressionConverter {

    /**
     * 간단한 표현식 매핑
     */
    private static final Map<String, String> SIMPLE_EXPRESSIONS = Map.of(
            "HOURLY", "cron(0 * * * ? *)",      // 매 시간 정각
            "DAILY", "cron(0 0 * * ? *)",       // 매일 UTC 00:00
            "WEEKLY", "cron(0 0 ? * MON *)"     // 매주 월요일 UTC 00:00
    );

    /**
     * Cron 표현식을 AWS EventBridge 형식으로 변환
     * <p>
     * 입력 형식:
     * <ul>
     *   <li>간단한 표현식: HOURLY, DAILY, WEEKLY</li>
     *   <li>커스텀 Cron: cron(0 12 * * ? *)</li>
     * </ul>
     * </p>
     *
     * @param cronExpression Cron 표현식
     * @return AWS EventBridge Cron 형식
     * @throws IllegalArgumentException 잘못된 형식인 경우
     */
    public String toAwsCron(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression cannot be null or empty");
        }

        String trimmed = cronExpression.trim().toUpperCase();

        // 간단한 표현식 처리
        if (SIMPLE_EXPRESSIONS.containsKey(trimmed)) {
            return SIMPLE_EXPRESSIONS.get(trimmed);
        }

        // 이미 cron(...) 형식인 경우 그대로 반환
        if (trimmed.startsWith("CRON(") && trimmed.endsWith(")")) {
            validateAwsCronFormat(trimmed);
            return trimmed.toLowerCase();
        }

        // 형식이 맞지 않으면 예외 발생
        throw new IllegalArgumentException(
                "Invalid cron expression: " + cronExpression
                        + ". Expected: HOURLY, DAILY, WEEKLY, or cron(...)"
        );
    }

    /**
     * AWS Cron 형식 검증
     * <p>
     * AWS EventBridge Cron은 6개 필드가 필요합니다:
     * cron(분 시 일 월 요일 년도)
     * </p>
     *
     * @param awsCron AWS Cron 표현식
     * @throws IllegalArgumentException 형식이 올바르지 않은 경우
     */
    private void validateAwsCronFormat(String awsCron) {
        if (!awsCron.startsWith("CRON(") || !awsCron.endsWith(")")) {
            throw new IllegalArgumentException(
                    "AWS Cron must start with 'cron(' and end with ')'"
            );
        }

        String cronBody = awsCron.substring(5, awsCron.length() - 1);
        String[] fields = cronBody.split("\\s+");

        if (fields.length != 6) {
            throw new IllegalArgumentException(
                    "AWS Cron requires 6 fields: cron(minute hour day-of-month month day-of-week year). "
                            + "Found: " + fields.length + " fields"
            );
        }
    }

    /**
     * 시간 기반 Cron 생성 (테스트용)
     * <p>
     * 매일 지정된 UTC 시간에 실행되는 Cron 표현식을 생성합니다.
     * </p>
     *
     * @param hour UTC 시간 (0-23)
     * @return AWS Cron 표현식
     */
    public String createDailyCron(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        return String.format("cron(0 %d * * ? *)", hour);
    }

    /**
     * 주간 Cron 생성 (테스트용)
     * <p>
     * 매주 지정된 요일 + 시간에 실행되는 Cron 표현식을 생성합니다.
     * </p>
     *
     * @param dayOfWeek 요일 (MON, TUE, WED, THU, FRI, SAT, SUN)
     * @param hour      UTC 시간 (0-23)
     * @return AWS Cron 표현식
     */
    public String createWeeklyCron(String dayOfWeek, int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        return String.format("cron(0 %d ? * %s *)", hour, dayOfWeek.toUpperCase());
    }
}
