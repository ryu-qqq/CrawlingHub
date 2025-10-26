package com.ryuqq.crawlinghub.domain.common;

import java.time.LocalDateTime;

/**
 * Domain Event 마커 인터페이스
 * <p>
 * 모든 Domain Event가 구현해야 하는 기본 인터페이스입니다.
 * 순수 Java로 구현하여 프레임워크 의존성을 제거합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시각을 반환합니다.
     *
     * @return 이벤트 발생 시각
     */
    LocalDateTime getOccurredAt();
}
