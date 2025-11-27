package com.ryuqq.crawlinghub.application.crawl.component;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 크롤러 제공자
 *
 * <p>CrawlTaskType에 따라 적절한 크롤러를 제공하는 전략 패턴 구현체. Spring의 의존성 주입을 통해 모든 Crawler 구현체를 자동 수집.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * CrawlTaskType type = CrawlTaskType.META;
 * Crawler crawler = crawlerProvider.getCrawler(type);
 * CrawlResult result = crawler.crawl(context);
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlerProvider {

    private final Map<CrawlTaskType, Crawler> crawlerMap;

    public CrawlerProvider(List<Crawler> crawlers) {
        this.crawlerMap =
                crawlers.stream()
                        .collect(Collectors.toMap(Crawler::supportedType, Function.identity()));
    }

    /**
     * CrawlTaskType에 해당하는 크롤러 반환
     *
     * @param type 크롤링 타입
     * @return 해당 타입의 크롤러
     * @throws IllegalArgumentException 지원하지 않는 타입인 경우
     */
    public Crawler getCrawler(CrawlTaskType type) {
        Crawler crawler = crawlerMap.get(type);
        if (crawler == null) {
            throw new IllegalArgumentException("No crawler found for type: " + type);
        }
        return crawler;
    }

    /**
     * 해당 타입의 크롤러 지원 여부 확인
     *
     * @param type 확인할 크롤링 타입
     * @return 지원 여부
     */
    public boolean supports(CrawlTaskType type) {
        return crawlerMap.containsKey(type);
    }
}
