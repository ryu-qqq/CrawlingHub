package com.ryuqq.crawlinghub.domain.schedule.vo;

/**
 * 스케줄러 이름 Value Object
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>최대 길이: 50자
 *   <li>특수문자, 영어, 한글 모두 허용
 *   <li>셀러별 중복 불가 (외부 검증 필요)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerName(String value) {

    private static final int MAX_LENGTH = 50;

    /** Compact Constructor (검증 로직) */
    public SchedulerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("스케줄러 이름은 null이거나 빈 문자열일 수 없습니다.");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "스케줄러 이름은 " + MAX_LENGTH + "자를 초과할 수 없습니다: " + value.length());
        }
    }

    /**
     * 값 기반 생성
     *
     * @param value 스케줄러 이름 (50자 이내)
     * @return SchedulerName
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static SchedulerName of(String value) {
        return new SchedulerName(value);
    }

    /**
     * 동일한 이름인지 확인
     *
     * @param other 비교 대상 이름
     * @return 동일 여부
     */
    public boolean isSameAs(String other) {
        return this.value.equals(other);
    }
}
