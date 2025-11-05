package com.ryuqq.crawlinghub.application.schedule.validator;

import org.quartz.CronExpression;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Cron 표현식 검증 및 계산
 * <p>
 * Quartz 라이브러리 기반 검증 (Application Layer)
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Component
public class CronExpressionValidator {

    /**
     * Cron 표현식 유효성 검증
     *
     * @param expression Cron 표현식 문자열
     * @return 유효하면 true
     */
    public boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        return CronExpression.isValidExpression(expression.trim());
    }

    /**
     * 다음 실행 시간 계산
     *
     * @param expression Cron 표현식 문자열
     * @param from       기준 시간
     * @return 다음 실행 시간
     * @throws IllegalArgumentException 유효하지 않은 Cron 표현식인 경우
     */
    public LocalDateTime calculateNextExecution(String expression, LocalDateTime from) {
        try {
            CronExpression cron = new CronExpression(expression.trim());
            Date nextFire = cron.getNextValidTimeAfter(
                Date.from(from.atZone(ZoneId.systemDefault()).toInstant())
            );

            if (nextFire == null) {
                throw new IllegalArgumentException("다음 실행 시간을 계산할 수 없습니다: " + expression);
            }

            return LocalDateTime.ofInstant(nextFire.toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            throw new IllegalArgumentException("유효하지 않은 Cron 표현식입니다: " + expression, e);
        }
    }

    /**
     * 현재 시간 기준 다음 실행 시간 계산
     *
     * @param expression Cron 표현식 문자열
     * @return 다음 실행 시간
     */
    public LocalDateTime calculateNextExecution(String expression) {
        return calculateNextExecution(expression, LocalDateTime.now());
    }
}
