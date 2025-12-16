package com.ryuqq.crawlinghub.application.sync.port.in.command;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;

/**
 * 외부 서버 동기화 완료 처리 UseCase
 *
 * <p>외부 서버에서 동기화 완료 웹훅을 받아 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CompleteSyncUseCase {

    /**
     * 외부 서버 동기화 완료 처리
     *
     * <p>CrawledProduct의 동기화 상태를 완료로 변경하고 외부 상품 ID를 저장합니다.
     *
     * @param crawledProductId CrawledProduct ID
     * @param externalProductId 외부 서버에서 부여받은 상품 ID
     */
    void complete(CrawledProductId crawledProductId, Long externalProductId);
}
