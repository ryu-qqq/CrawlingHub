package com.ryuqq.crawlinghub.application.product.internal.processor;

import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlType별 CrawledRawProcessor를 제공하는 Provider
 *
 * <p>Spring이 주입한 모든 CrawledRawProcessor를 EnumMap으로 관리하여 O(1) 조회를 보장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawProcessorProvider {

    private final Map<CrawlType, CrawledRawProcessor> processorMap;

    public CrawledRawProcessorProvider(List<CrawledRawProcessor> processors) {
        this.processorMap = new EnumMap<>(CrawlType.class);
        for (CrawledRawProcessor processor : processors) {
            processorMap.put(processor.supportedType(), processor);
        }
    }

    public CrawledRawProcessor getProcessor(CrawlType type) {
        CrawledRawProcessor processor = processorMap.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("지원하지 않는 CrawlType: " + type);
        }
        return processor;
    }
}
