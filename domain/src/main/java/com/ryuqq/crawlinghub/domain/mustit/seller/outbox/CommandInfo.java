package com.ryuqq.crawlinghub.domain.mustit.seller.outbox;

import java.util.Objects;

/**
 * Orchestrator Command 정보 (불변 객체)
 * <p>
 * Transactional Outbox Pattern에서 사용되는 Command 메타데이터입니다.
 * Orchestrator SDK의 Command 생성에 필요한 정보를 담고 있습니다.
 * </p>
 *
 * @param domain    도메인 (예: seller-crawl-schedule)
 * @param eventType 이벤트 타입 (예: SCHEDULE.CREATE.REQUEST)
 * @param bizKey    비즈니스 키 (예: seller-123)
 * @param idemKey   멱등성 키 (중복 실행 방지)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record CommandInfo(
        String domain,
        String eventType,
        String bizKey,
        String idemKey
) {
    /**
     * Compact Constructor for Validation
     */
    public CommandInfo {
        Objects.requireNonNull(domain, "domain must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(bizKey, "bizKey must not be null");
        Objects.requireNonNull(idemKey, "idemKey must not be null");
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param domain    도메인
     * @param eventType 이벤트 타입
     * @param bizKey    비즈니스 키
     * @param idemKey   멱등성 키
     * @return CommandInfo 인스턴스
     */
    public static CommandInfo of(
            String domain,
            String eventType,
            String bizKey,
            String idemKey
    ) {
        return new CommandInfo(domain, eventType, bizKey, idemKey);
    }
}
