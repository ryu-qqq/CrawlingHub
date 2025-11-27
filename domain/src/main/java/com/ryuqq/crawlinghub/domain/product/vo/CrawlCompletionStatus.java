package com.ryuqq.crawlinghub.domain.product.vo;

import java.time.LocalDateTime;

/**
 * 크롤링 완료 상태 VO
 *
 * <p>MINI_SHOP, DETAIL, OPTION 각 크롤링 타입별 완료 여부와 시간을 추적합니다.
 * <p>외부 서버 동기화는 세 가지 크롤링이 모두 최소 한 번 이상 완료되어야 가능합니다.
 *
 * @param miniShopCrawledAt MINI_SHOP 크롤링 완료 시각 (null이면 미완료)
 * @param detailCrawledAt DETAIL 크롤링 완료 시각 (null이면 미완료)
 * @param optionCrawledAt OPTION 크롤링 완료 시각 (null이면 미완료)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlCompletionStatus(
        LocalDateTime miniShopCrawledAt,
        LocalDateTime detailCrawledAt,
        LocalDateTime optionCrawledAt) {

    /**
     * 초기 상태 생성 (모두 미완료)
     */
    public static CrawlCompletionStatus initial() {
        return new CrawlCompletionStatus(null, null, null);
    }

    /**
     * MINI_SHOP 크롤링 완료 표시
     *
     * @param crawledAt 크롤링 완료 시각
     * @return 갱신된 상태
     */
    public CrawlCompletionStatus withMiniShopCrawled(LocalDateTime crawledAt) {
        return new CrawlCompletionStatus(crawledAt, this.detailCrawledAt, this.optionCrawledAt);
    }

    /**
     * DETAIL 크롤링 완료 표시
     *
     * @param crawledAt 크롤링 완료 시각
     * @return 갱신된 상태
     */
    public CrawlCompletionStatus withDetailCrawled(LocalDateTime crawledAt) {
        return new CrawlCompletionStatus(this.miniShopCrawledAt, crawledAt, this.optionCrawledAt);
    }

    /**
     * OPTION 크롤링 완료 표시
     *
     * @param crawledAt 크롤링 완료 시각
     * @return 갱신된 상태
     */
    public CrawlCompletionStatus withOptionCrawled(LocalDateTime crawledAt) {
        return new CrawlCompletionStatus(this.miniShopCrawledAt, this.detailCrawledAt, crawledAt);
    }

    /**
     * MINI_SHOP 크롤링이 완료되었는지 확인
     */
    public boolean isMiniShopCrawled() {
        return miniShopCrawledAt != null;
    }

    /**
     * DETAIL 크롤링이 완료되었는지 확인
     */
    public boolean isDetailCrawled() {
        return detailCrawledAt != null;
    }

    /**
     * OPTION 크롤링이 완료되었는지 확인
     */
    public boolean isOptionCrawled() {
        return optionCrawledAt != null;
    }

    /**
     * 모든 크롤링이 완료되었는지 확인
     *
     * <p>외부 서버 동기화 가능 여부 판단에 사용
     *
     * @return 모든 크롤링(MINI_SHOP, DETAIL, OPTION)이 완료되면 true
     */
    public boolean isAllCrawled() {
        return isMiniShopCrawled() && isDetailCrawled() && isOptionCrawled();
    }

    /**
     * 외부 서버 동기화 가능 여부 확인
     *
     * <p>세 가지 크롤링이 모두 한 번 이상 완료되어야 외부 서버로 상품 등록/갱신 가능
     *
     * @return 동기화 가능하면 true
     */
    public boolean canSyncToExternalServer() {
        return isAllCrawled();
    }

    /**
     * 완료된 크롤링 타입 개수
     *
     * @return 0~3 사이 값
     */
    public int getCompletedCount() {
        int count = 0;
        if (isMiniShopCrawled()) count++;
        if (isDetailCrawled()) count++;
        if (isOptionCrawled()) count++;
        return count;
    }

    /**
     * 미완료 크롤링 타입 설명
     *
     * @return 미완료 타입 목록 (예: "DETAIL, OPTION")
     */
    public String getPendingCrawlTypes() {
        StringBuilder sb = new StringBuilder();
        if (!isMiniShopCrawled()) {
            sb.append("MINI_SHOP");
        }
        if (!isDetailCrawled()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("DETAIL");
        }
        if (!isOptionCrawled()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("OPTION");
        }
        return sb.toString();
    }

    /**
     * 가장 최근 크롤링 시각
     *
     * @return 가장 최근 크롤링 완료 시각 (모두 null이면 null)
     */
    public LocalDateTime getLastCrawledAt() {
        LocalDateTime latest = null;
        if (miniShopCrawledAt != null) {
            latest = miniShopCrawledAt;
        }
        if (detailCrawledAt != null && (latest == null || detailCrawledAt.isAfter(latest))) {
            latest = detailCrawledAt;
        }
        if (optionCrawledAt != null && (latest == null || optionCrawledAt.isAfter(latest))) {
            latest = optionCrawledAt;
        }
        return latest;
    }

    /**
     * 가장 오래된 크롤링 시각 (데이터 신선도 판단)
     *
     * @return 가장 오래된 크롤링 완료 시각 (하나라도 null이면 null)
     */
    public LocalDateTime getOldestCrawledAt() {
        if (!isAllCrawled()) {
            return null;
        }
        LocalDateTime oldest = miniShopCrawledAt;
        if (detailCrawledAt.isBefore(oldest)) {
            oldest = detailCrawledAt;
        }
        if (optionCrawledAt.isBefore(oldest)) {
            oldest = optionCrawledAt;
        }
        return oldest;
    }
}
