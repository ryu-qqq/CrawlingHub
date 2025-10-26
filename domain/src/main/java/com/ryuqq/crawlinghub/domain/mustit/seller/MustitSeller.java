package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 머스트잇 셀러를 표현하는 Aggregate Root
 * <p>
 * 셀러의 기본 정보와 크롤링 설정을 관리합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class MustitSeller {

    private final String sellerId;
    private final String name;  // 불변: 머스트잇 셀러명은 한번 등록하면 변경 불가
    private boolean isActive;
    private CrawlInterval crawlInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 새로운 머스트잇 셀러를 생성합니다.
     *
     * @param sellerId 셀러 고유 ID
     * @param name 셀러명
     * @param crawlInterval 크롤링 주기
     * @throws IllegalArgumentException sellerId 또는 name이 null이거나 빈 문자열인 경우
     */
    public MustitSeller(String sellerId, String name, CrawlInterval crawlInterval) {
        validateSellerId(sellerId);
        validateName(name);

        this.sellerId = sellerId;
        this.name = name;
        this.isActive = true;  // 기본값: 활성
        this.crawlInterval = Objects.requireNonNull(crawlInterval, "crawlInterval must not be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기존 셀러 정보를 재구성하는 정적 팩토리 메서드 (Persistence에서 로드 시 사용).
     *
     * @param basicInfo     기본 정보 (sellerId, name, isActive)
     * @param crawlInterval 크롤링 주기
     * @param timeInfo      시간 정보 (createdAt, updatedAt)
     * @return 재구성된 MustitSeller Aggregate
     */
    public static MustitSeller reconstitute(
            SellerBasicInfo basicInfo,
            CrawlInterval crawlInterval,
            SellerTimeInfo timeInfo
    ) {
        MustitSeller seller = new MustitSeller(
                basicInfo.sellerId(),
                basicInfo.name(),
                crawlInterval
        );
        seller.isActive = basicInfo.isActive();
        seller.createdAt = timeInfo.createdAt();
        seller.updatedAt = timeInfo.updatedAt();
        return seller;
    }

    /**
     * 셀러 ID 유효성 검증
     *
     * @param sellerIdValue 검증할 셀러 ID
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     */
    private void validateSellerId(String sellerIdValue) {
        if (sellerIdValue == null || sellerIdValue.isBlank()) {
            throw new IllegalArgumentException("sellerId must not be null or blank");
        }
    }

    /**
     * 셀러명 유효성 검증
     *
     * @param nameValue 검증할 셀러명
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     */
    private void validateName(String nameValue) {
        if (nameValue == null || nameValue.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }

    /**
     * 크롤링 주기를 변경합니다.
     *
     * @param newCrawlInterval 새로운 크롤링 주기
     */
    public void updateCrawlInterval(CrawlInterval newCrawlInterval) {
        this.crawlInterval = Objects.requireNonNull(newCrawlInterval, "newCrawlInterval must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 셀러를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 셀러를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 셀러 ID를 반환합니다.
     *
     * @return 셀러 ID
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * 셀러명을 반환합니다.
     *
     * @return 셀러명
     */
    public String getName() {
        return name;
    }

    /**
     * 활성 상태를 반환합니다.
     *
     * @return 활성 상태
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 크롤링 주기를 반환합니다.
     *
     * @return 크롤링 주기
     */
    public CrawlInterval getCrawlInterval() {
        return crawlInterval;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각을 반환합니다.
     *
     * @return 수정 시각
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MustitSeller that = (MustitSeller) o;
        return Objects.equals(sellerId, that.sellerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellerId);
    }

    @Override
    public String toString() {
        return "MustitSeller{"
                + "sellerId='" + sellerId + '\''
                + ", name='" + name + '\''
                + ", isActive=" + isActive
                + ", crawlInterval=" + crawlInterval
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
}
