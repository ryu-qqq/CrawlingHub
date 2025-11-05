package com.ryuqq.crawlinghub.domain.change;

import java.util.Objects;

/**
 * 변경 상세 정보 Value Object
 */
public class ChangeData {

    private final String details;

    private ChangeData(String details) {
        validateDetails(details);
        this.details = details;
    }

    public static ChangeData of(String details) {
        return new ChangeData(details);
    }

    private static void validateDetails(String details) {
        if (details == null || details.isBlank()) {
            throw new IllegalArgumentException("변경 상세 정보는 필수입니다");
        }
    }

    public String getValue() {
        return details;
    }

    public boolean isSameAs(ChangeData other) {
        if (other == null) {
            return false;
        }
        return this.details.equals(other.details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangeData that = (ChangeData) o;
        return Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details);
    }

    @Override
    public String toString() {
        return details;
    }
}
