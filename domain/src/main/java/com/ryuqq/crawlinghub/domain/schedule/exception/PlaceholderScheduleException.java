package com.ryuqq.crawlinghub.domain.schedule.exception;

/**
 * Placeholder Schedule Exception
 *
 * <p>Sealed Interface의 permits 절에 최소 하나의 구현이 필요하므로 임시로 생성한 예외입니다.</p>
 * <p>향후 실제 Schedule 예외가 추가되면 이 클래스는 제거될 수 있습니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
final class PlaceholderScheduleException extends RuntimeException implements ScheduleException {
    @Override
    public String code() {
        return "SCHEDULE-PLACEHOLDER";
    }

    @Override
    public String message() {
        return "Placeholder exception";
    }

    @Override
    public java.util.Map<String, Object> args() {
        return java.util.Map.of();
    }
}

