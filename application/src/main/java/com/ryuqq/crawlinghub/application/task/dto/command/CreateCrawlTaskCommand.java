package com.ryuqq.crawlinghub.application.task.dto.command;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;

/**
 * CrawlTask 동적 생성 Command DTO
 *
 * <p>크롤러가 크롤링 결과에서 후속 태스크를 동적으로 생성할 때 사용.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>MetaCrawler → MiniShop 태스크 생성
 *   <li>MiniShopCrawler → Detail, Option 태스크 생성
 *   <li>SearchCrawler → 다음 SEARCH 페이지, Detail, Option 태스크 생성
 * </ul>
 *
 * @param crawlSchedulerId 원본 스케줄러 ID (연관 관계 유지)
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명 (API 조회 시 필요)
 * @param taskType 생성할 태스크 타입
 * @param targetId 크롤링 대상 ID (상품번호 등, nullable)
 * @param endpoint 커스텀 엔드포인트 URL (SEARCH 타입 등에서 사용, nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CreateCrawlTaskCommand(
        Long crawlSchedulerId,
        Long sellerId,
        String mustItSellerName,
        CrawlTaskType taskType,
        Long targetId,
        String endpoint) {

    public CreateCrawlTaskCommand {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (mustItSellerName == null || mustItSellerName.isBlank()) {
            throw new IllegalArgumentException("mustItSellerName은 null이거나 빈 값일 수 없습니다.");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("taskType은 null일 수 없습니다.");
        }
    }

    /** 기존 4-arg 생성자 호환성 유지용 팩토리 */
    public static CreateCrawlTaskCommand of(
            Long crawlSchedulerId, Long sellerId, CrawlTaskType taskType, Long targetId) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, "", taskType, targetId, null);
    }

    /**
     * META 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forMeta(
            Long crawlSchedulerId, Long sellerId, String mustItSellerName) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, mustItSellerName, CrawlTaskType.META, null, null);
    }

    /**
     * MINI_SHOP 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @param pageNumber 페이지 번호 (1부터 시작)
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forMiniShop(
            Long crawlSchedulerId, Long sellerId, String mustItSellerName, Long pageNumber) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId,
                sellerId,
                mustItSellerName,
                CrawlTaskType.MINI_SHOP,
                pageNumber,
                null);
    }

    /**
     * DETAIL 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @param itemNo 상품 번호
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forDetail(
            Long crawlSchedulerId, Long sellerId, String mustItSellerName, Long itemNo) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, mustItSellerName, CrawlTaskType.DETAIL, itemNo, null);
    }

    /**
     * OPTION 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @param itemNo 상품 번호
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forOption(
            Long crawlSchedulerId, Long sellerId, String mustItSellerName, Long itemNo) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, mustItSellerName, CrawlTaskType.OPTION, itemNo, null);
    }

    /**
     * SEARCH 다음 페이지 태스크 생성용 팩토리 메서드
     *
     * <p>무한스크롤 방식의 Search API에서 nextApiUrl로 다음 페이지 태스크 생성
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @param nextApiUrl 다음 페이지 API URL
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forSearchNextPage(
            Long crawlSchedulerId, Long sellerId, String mustItSellerName, String nextApiUrl) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, mustItSellerName, CrawlTaskType.SEARCH, null, nextApiUrl);
    }
}
