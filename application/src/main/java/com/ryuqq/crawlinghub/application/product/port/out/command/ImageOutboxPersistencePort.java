package com.ryuqq.crawlinghub.application.product.port.out.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import java.util.List;

/**
 * 이미지 업로드 Outbox 저장 Port (Port Out - Command)
 *
 * <p>ImageOutboxManager에서만 사용됩니다.
 *
 * <p>이미지 업로드 요청의 트랜잭션 경계를 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ImageOutboxPersistencePort {

    /**
     * ImageOutbox 저장
     *
     * @param outbox 저장할 Outbox
     */
    void persist(CrawledProductImageOutbox outbox);

    /**
     * ImageOutbox 일괄 저장
     *
     * @param outboxes 저장할 Outbox 목록
     */
    void persistAll(List<CrawledProductImageOutbox> outboxes);

    /**
     * ImageOutbox 상태 업데이트
     *
     * @param outbox 업데이트할 Outbox
     */
    void update(CrawledProductImageOutbox outbox);
}
