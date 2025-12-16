package com.ryuqq.crawlinghub.application.sync.port.in.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;

/**
 * 외부 서버 동기화 요청 UseCase
 *
 * <p>CrawledProduct가 동기화 가능한 상태인지 확인하고 Outbox를 생성합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RequestSyncUseCase {

    /**
     * 외부 서버 동기화 가능 여부 확인 및 요청
     *
     * <p>상품이 동기화 가능한 상태(모든 크롤링 완료 + 이미지 업로드 완료)이면 SyncOutbox를 생성하고 이벤트를 발행합니다.
     *
     * @param product 동기화할 CrawledProduct
     */
    void requestIfReady(CrawledProduct product);
}
