package com.ryuqq.crawlinghub.application.crawl.orchestration.port.in;

import com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command.MiniShopResultCommand;

/**
 * 미니샵 크롤링 결과 처리 UseCase
 *
 * <p>총 상품 수를 추출하고 후속 태스크(페이징, 상세, 옵션)를 생성합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface ProcessMiniShopResultUseCase {

    /**
     * 미니샵 결과 처리
     *
     * @param command 미니샵 API 응답 데이터
     */
    void execute(MiniShopResultCommand command);
}
