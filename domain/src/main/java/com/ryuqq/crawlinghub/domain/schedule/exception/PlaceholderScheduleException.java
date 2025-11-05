package com.ryuqq.crawlinghub.domain.schedule.exception;


import java.util.Map;

/**
 * Placeholder Schedule Exception
 *
 * <p>Sealed Abstract Class의 permits 절에 최소 하나의 구현이 필요하므로 임시로 생성한 예외입니다.</p>
 * <p>향후 실제 Schedule 예외가 추가되면 이 클래스는 제거될 수 있습니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public final class PlaceholderScheduleException extends ScheduleException {

    /**
     * PlaceholderScheduleException 생성자
     */
    public PlaceholderScheduleException() {
        super("Placeholder exception");
    }

    @Override
    public String code() {
        return ScheduleErrorCode.SCHEDULE_PLACEHOLDER.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}

