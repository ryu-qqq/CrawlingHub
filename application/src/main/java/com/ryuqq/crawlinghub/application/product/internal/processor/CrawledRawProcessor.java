package com.ryuqq.crawlinghub.application.product.internal.processor;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;

/**
 * CrawledRaw 타입별 가공 전략 인터페이스
 *
 * <p>CrawledRaw를 직접 받아 역직렬화 + 가공을 내부에서 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledRawProcessor {

    CrawlType supportedType();

    void process(CrawledRaw raw);
}
