package com.ryuqq.crawlinghub.application.crawl.processor;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlResultProcessor Provider
 *
 * <p>CrawlTaskType에 따른 적절한 CrawlResultProcessor를 제공하는 전략 패턴 Provider.
 *
 * <p><strong>지원 타입</strong>:
 *
 * <ul>
 *   <li>META → MetaCrawlResultProcessor
 *   <li>MINI_SHOP → MiniShopCrawlResultProcessor
 *   <li>DETAIL → DetailCrawlResultProcessor
 *   <li>OPTION → OptionCrawlResultProcessor
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlResultProcessorProvider {

    private final Map<CrawlTaskType, CrawlResultProcessor> processorMap;

    public CrawlResultProcessorProvider(List<CrawlResultProcessor> processors) {
        this.processorMap = new EnumMap<>(CrawlTaskType.class);
        for (CrawlResultProcessor processor : processors) {
            this.processorMap.put(processor.supportedType(), processor);
        }
    }

    /**
     * 태스크 타입에 맞는 프로세서 반환
     *
     * @param taskType 크롤 태스크 타입
     * @return CrawlResultProcessor
     * @throws IllegalArgumentException 지원하지 않는 타입인 경우
     */
    public CrawlResultProcessor getProcessor(CrawlTaskType taskType) {
        CrawlResultProcessor processor = processorMap.get(taskType);
        if (processor == null) {
            throw new IllegalArgumentException(
                    "지원하지 않는 CrawlTaskType입니다: " + taskType);
        }
        return processor;
    }

    /**
     * 특정 타입 지원 여부 확인
     *
     * @param taskType 크롤 태스크 타입
     * @return 지원하면 true
     */
    public boolean supports(CrawlTaskType taskType) {
        return processorMap.containsKey(taskType);
    }
}
