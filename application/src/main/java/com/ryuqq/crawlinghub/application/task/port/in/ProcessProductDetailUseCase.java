package com.ryuqq.crawlinghub.application.task.port.in;

import com.ryuqq.crawlinghub.application.task.assembler.command.ProductDetailCommand;

/**
 * 상품 상세 크롤링 결과 처리 UseCase
 *
 * <p>상품 데이터를 저장하고 변경 감지 및 완성도 체크를 수행합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface ProcessProductDetailUseCase {

    /**
     * 상품 상세 결과 처리
     *
     * @param command 상품 상세 API 응답 데이터
     */
    void execute(ProductDetailCommand command);
}
