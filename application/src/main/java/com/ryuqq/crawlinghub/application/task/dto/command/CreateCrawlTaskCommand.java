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
 * </ul>
 *
 * @param crawlSchedulerId 원본 스케줄러 ID (연관 관계 유지)
 * @param sellerId 셀러 ID
 * @param taskType 생성할 태스크 타입
 * @param targetId 크롤링 대상 ID (상품번호 등, nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CreateCrawlTaskCommand(
        Long crawlSchedulerId, Long sellerId, CrawlTaskType taskType, Long targetId) {
    public CreateCrawlTaskCommand {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("taskType은 null일 수 없습니다.");
        }
    }

    /**
     * META 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forMeta(Long crawlSchedulerId, Long sellerId) {
        return new CreateCrawlTaskCommand(crawlSchedulerId, sellerId, CrawlTaskType.META, null);
    }

    /**
     * MINI_SHOP 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param pageNumber 페이지 번호 (1부터 시작)
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forMiniShop(
            Long crawlSchedulerId, Long sellerId, Long pageNumber) {
        return new CreateCrawlTaskCommand(
                crawlSchedulerId, sellerId, CrawlTaskType.MINI_SHOP, pageNumber);
    }

    /**
     * DETAIL 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param itemNo 상품 번호
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forDetail(
            Long crawlSchedulerId, Long sellerId, Long itemNo) {
        return new CreateCrawlTaskCommand(crawlSchedulerId, sellerId, CrawlTaskType.DETAIL, itemNo);
    }

    /**
     * OPTION 태스크 생성용 팩토리 메서드
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param itemNo 상품 번호
     * @return CreateCrawlTaskCommand
     */
    public static CreateCrawlTaskCommand forOption(
            Long crawlSchedulerId, Long sellerId, Long itemNo) {
        return new CreateCrawlTaskCommand(crawlSchedulerId, sellerId, CrawlTaskType.OPTION, itemNo);
    }
}
