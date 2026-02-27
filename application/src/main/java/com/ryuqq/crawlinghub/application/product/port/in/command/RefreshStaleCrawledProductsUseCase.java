package com.ryuqq.crawlinghub.application.product.port.in.command;

/**
 * 갱신이 오래된 CrawledProduct에 대해 DETAIL + OPTION 크롤 태스크를 생성하는 UseCase
 *
 * <p>updatedAt이 가장 오래된 상품부터 주기적으로 재크롤링 태스크를 생성하여 재고/가격을 갱신합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RefreshStaleCrawledProductsUseCase {

    /**
     * stale 상품에 대한 DETAIL + OPTION 태스크 일괄 생성
     *
     * @param batchSize 한 번에 처리할 상품 수
     * @return 생성된 태스크 커맨드 수
     */
    int execute(int batchSize);
}
