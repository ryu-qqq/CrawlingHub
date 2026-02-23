package com.ryuqq.crawlinghub.application.execution.internal.crawler;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;

/**
 * 크롤러 추상 클래스
 *
 * <p>모든 크롤러의 기본 템플릿을 제공하는 추상 클래스. CrawlTaskType에 따라 적절한 크롤링 로직을 구현.
 *
 * <p><strong>구현 클래스</strong>:
 *
 * <ul>
 *   <li>{@link SearchCrawler} - 무한스크롤 검색 크롤링
 *   <li>{@link DetailCrawler} - 상품 상세 정보 크롤링
 *   <li>{@link OptionCrawler} - 상품 옵션 정보 크롤링
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public abstract class Crawler {

    /**
     * 크롤러가 지원하는 CrawlTaskType 반환
     *
     * @return 지원하는 CrawlTaskType
     */
    public abstract CrawlTaskType supportedType();

    /**
     * 크롤링 실행
     *
     * <p>하위 클래스에서 구체적인 크롤링 로직 구현
     *
     * @param context 크롤링 컨텍스트 정보
     * @return 크롤링 결과
     */
    public abstract CrawlResult crawl(CrawlContext context);

    /**
     * 크롤러가 해당 타입을 지원하는지 확인
     *
     * @param type 확인할 CrawlTaskType
     * @return 지원 여부
     */
    public boolean supports(CrawlTaskType type) {
        return supportedType() == type;
    }
}
