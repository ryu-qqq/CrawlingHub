package com.ryuqq.crawlinghub.domain.mustit.seller;

import com.ryuqq.crawlinghub.domain.common.AggregateRoot;
import com.ryuqq.crawlinghub.domain.mustit.seller.event.SellerCrawlIntervalChangedEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 머스트잇 셀러를 표현하는 Aggregate Root
 * <p>
 * 셀러의 기본 정보와 크롤링 설정을 관리합니다.
 * Domain Event를 발행하여 크롤링 주기 변경 등의 이벤트를 외부에 알립니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class MustitSeller extends AggregateRoot {

    private Long id;  // Persistence Layer에서 reconstitute 시 주입 (null 가능)
    private final String sellerId;
    private final String name;  // 불변: 머스트잇 셀러명은 한번 등록하면 변경 불가
    private boolean isActive;
    private CrawlInterval crawlInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 변경 감지용 원본 상태 저장
    private boolean originalIsActive;
    private CrawlInterval originalCrawlInterval;

    /**
     * 새로운 머스트잇 셀러를 생성합니다.
     *
     * @param sellerId 셀러 고유 ID
     * @param name 셀러명
     * @param crawlInterval 크롤링 주기
     * @throws IllegalArgumentException sellerId 또는 name이 null이거나 빈 문자열인 경우
     */
    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "생성자 검증은 객체 생성 전 필수입니다. Finalizer 공격은 final 클래스로 방어됩니다."
    )
    public MustitSeller(String sellerId, String name, CrawlInterval crawlInterval) {
        validateSellerId(sellerId);
        validateName(name);

        this.sellerId = sellerId;
        this.name = name;
        this.isActive = true;  // 기본값: 활성
        this.crawlInterval = Objects.requireNonNull(crawlInterval, "crawlInterval must not be null");
        this.originalIsActive = true;  // 생성 시점의 활성 상태 저장 (항상 true)
        this.originalCrawlInterval = crawlInterval;  // 생성 시점의 크롤링 주기 저장
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기존 셀러 정보를 재구성하는 정적 팩토리 메서드 (Persistence에서 로드 시 사용).
     *
     * @param basicInfo     기본 정보 (id, sellerId, name, isActive)
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
        seller.id = basicInfo.id();  // Persistence PK 주입
        seller.isActive = basicInfo.isActive();
        seller.createdAt = timeInfo.createdAt();
        seller.updatedAt = timeInfo.updatedAt();
        seller.originalIsActive = basicInfo.isActive();  // 로드 시점의 활성 상태를 원본으로 저장
        seller.originalCrawlInterval = crawlInterval;  // 로드 시점의 크롤링 주기를 원본으로 저장
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
     * 크롤링 주기가 실제로 변경되면 SellerCrawlIntervalChangedEvent를 발행합니다.
     * <p>
     * 신규 생성 시점(id가 null)에는 이벤트를 발행하지 않습니다.
     * Persistence에 저장된 후(id가 설정된 후)에만 이벤트가 발행됩니다.
     * </p>
     *
     * @param newCrawlInterval 새로운 크롤링 주기
     */
    public void updateCrawlInterval(CrawlInterval newCrawlInterval) {
        Objects.requireNonNull(newCrawlInterval, "newCrawlInterval must not be null");

        CrawlInterval oldInterval = this.crawlInterval;
        this.crawlInterval = newCrawlInterval;
        this.updatedAt = LocalDateTime.now();

        // 크롤링 주기가 실제로 변경되고, id가 있는 경우에만 Event 발행
        // (신규 생성 시점에는 id가 null이므로 이벤트를 발행하지 않음)
        if (!oldInterval.equals(newCrawlInterval) && this.id != null) {
            registerEvent(new SellerCrawlIntervalChangedEvent(
                    this.sellerId,
                    this.id,  // Long sellerPk (Persistence에서 로드된 경우에만 not null)
                    oldInterval,
                    newCrawlInterval
            ));
        }
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
     * 크롤링 주기 타입을 반환합니다.
     * Law of Demeter를 준수하기 위한 편의 메서드입니다.
     *
     * @return 크롤링 주기 타입
     */
    public CrawlIntervalType getCrawlIntervalType() {
        return crawlInterval.getIntervalType();
    }

    /**
     * 크롤링 주기 값을 반환합니다.
     * Law of Demeter를 준수하기 위한 편의 메서드입니다.
     *
     * @return 크롤링 주기 값
     */
    public int getCrawlIntervalValue() {
        return crawlInterval.getIntervalValue();
    }

    /**
     * Cron 표현식을 반환합니다.
     * Law of Demeter를 준수하기 위한 편의 메서드입니다.
     *
     * @return Cron 표현식
     */
    public String getCronExpression() {
        return crawlInterval.getCronExpression();
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

    /**
     * Aggregate가 변경되었는지 확인합니다.
     * 활성 상태 또는 크롤링 주기가 원본과 다르면 변경된 것으로 판단합니다.
     *
     * @return 변경 여부
     */
    public boolean isModified() {
        boolean activeChanged = (this.isActive != this.originalIsActive);
        boolean intervalChanged = !this.originalCrawlInterval.equals(this.crawlInterval);
        return activeChanged || intervalChanged;
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
