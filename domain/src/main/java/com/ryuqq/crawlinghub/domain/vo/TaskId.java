package com.ryuqq.crawlinghub.domain.vo;

import java.util.UUID;

/**
 * 크롤러 작업 식별자 Value Object
 *
 * <p>크롤링 작업의 고유 식별자를 표현합니다.</p>
 *
 * <p>UUID 기반으로 생성되어 고유성을 보장합니다.</p>
 *
 * @param value UUID 값
 */
public record TaskId(UUID value) {

    /**
     * 새로운 TaskId 생성
     *
     * @return 고유한 TaskId
     */
    public static TaskId generate() {
        return new TaskId(UUID.randomUUID());
    }
}
