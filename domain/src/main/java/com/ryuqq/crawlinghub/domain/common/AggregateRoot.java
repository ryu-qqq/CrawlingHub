package com.ryuqq.crawlinghub.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root 추상 클래스
 * <p>
 * Domain Event를 수집하고 관리하는 기본 기능을 제공합니다.
 * 순수 Java로 구현하여 프레임워크 의존성을 제거합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Domain Event를 등록합니다.
     * <p>
     * 등록된 이벤트는 Aggregate가 저장될 때 발행됩니다.
     * </p>
     *
     * @param event 등록할 Domain Event
     */
    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }

    /**
     * 등록된 모든 Domain Event를 반환합니다.
     * <p>
     * 반환되는 리스트는 불변(immutable)입니다.
     * </p>
     *
     * @return 등록된 Domain Event 리스트 (불변)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 등록된 모든 Domain Event를 제거합니다.
     * <p>
     * 이벤트 발행 후 호출되어 이벤트를 정리합니다.
     * </p>
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
