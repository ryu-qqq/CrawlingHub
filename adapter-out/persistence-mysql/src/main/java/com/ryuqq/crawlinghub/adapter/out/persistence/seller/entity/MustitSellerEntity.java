package com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 머스트잇 셀러 JPA Entity
 * <p>
 * Long FK 전략을 사용하여 관계 어노테이션을 배제하고
 * Long 타입의 FK만 사용합니다.
 * </p>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Entity
@Table(
        name = "mustit_seller",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_id", columnNames = "seller_id")
        },
        indexes = {
                @Index(name = "idx_seller_id", columnList = "seller_id"),
                @Index(name = "idx_is_active", columnList = "is_active")
        }
)
public class MustitSellerEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false, unique = true, length = 100)
    private String sellerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "interval_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CrawlIntervalType intervalType;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected MustitSellerEntity() {
        super();
    }

    /**
     * 전체 필드 생성자 (private)
     * <p>
     * Static Factory Method에서만 호출됩니다.
     * </p>
     *
     * @param id            PK ID
     * @param sellerId      셀러 ID
     * @param name          셀러명
     * @param active        활성 상태
     * @param intervalType  크롤링 주기 타입
     * @param intervalValue 크롤링 주기 값
     * @param cronExpression cron 표현식
     */
    private MustitSellerEntity(
            Long id,
            String sellerId,
            String name,
            Boolean active,
            CrawlIntervalType intervalType,
            Integer intervalValue,
            String cronExpression
    ) {
        super();
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.active = active;
        this.intervalType = intervalType;
        this.intervalValue = intervalValue;
        this.cronExpression = cronExpression;
    }

    /**
     * 신규 Entity 생성 (PK 없음)
     * <p>
     * 새로운 셀러를 생성할 때 사용합니다.
     * PK는 JPA가 자동으로 생성합니다.
     * </p>
     *
     * @param sellerId      셀러 ID
     * @param name          셀러명
     * @param active        활성 상태
     * @param intervalType  크롤링 주기 타입
     * @param intervalValue 크롤링 주기 값
     * @param cronExpression cron 표현식
     * @return 생성된 Entity
     * @throws IllegalArgumentException 파라미터 검증 실패 시
     */
    public static MustitSellerEntity create(
            String sellerId,
            String name,
            Boolean active,
            CrawlIntervalType intervalType,
            Integer intervalValue,
            String cronExpression
    ) {
        validateSellerId(sellerId);
        validateName(name);
        validateInterval(intervalType, intervalValue);
        validateCronExpression(cronExpression);

        return new MustitSellerEntity(
                null,
                sellerId,
                name,
                active,
                intervalType,
                intervalValue,
                cronExpression
        );
    }

    /**
     * 기존 Entity 재구성 (조회 후 복원)
     * <p>
     * DB에서 조회된 데이터로 Entity를 재구성할 때 사용합니다.
     * </p>
     *
     * @param id            PK ID
     * @param sellerId      셀러 ID
     * @param name          셀러명
     * @param active        활성 상태
     * @param intervalType  크롤링 주기 타입
     * @param intervalValue 크롤링 주기 값
     * @param cronExpression cron 표현식
     * @return 재구성된 Entity
     */
    public static MustitSellerEntity reconstitute(
            Long id,
            String sellerId,
            String name,
            Boolean active,
            CrawlIntervalType intervalType,
            Integer intervalValue,
            String cronExpression
    ) {
        return new MustitSellerEntity(
                id,
                sellerId,
                name,
                active,
                intervalType,
                intervalValue,
                cronExpression
        );
    }

    /**
     * sellerId 유효성 검증
     *
     * @param sellerId 검증할 셀러 ID
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열이거나 길이 초과인 경우
     */
    private static void validateSellerId(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("sellerId must not be null or blank");
        }
        if (sellerId.length() > 100) {
            throw new IllegalArgumentException("sellerId must not exceed 100 characters");
        }
    }

    /**
     * name 유효성 검증
     *
     * @param name 검증할 셀러명
     * @throws IllegalArgumentException name이 null이거나 빈 문자열이거나 길이 초과인 경우
     */
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("name must not exceed 255 characters");
        }
    }

    /**
     * interval 유효성 검증
     *
     * @param intervalType  크롤링 주기 타입
     * @param intervalValue 크롤링 주기 값
     * @throws IllegalArgumentException 파라미터가 null이거나 intervalValue가 양수가 아닌 경우
     */
    private static void validateInterval(CrawlIntervalType intervalType, Integer intervalValue) {
        if (intervalType == null) {
            throw new IllegalArgumentException("intervalType must not be null");
        }
        if (intervalValue == null || intervalValue <= 0) {
            throw new IllegalArgumentException("intervalValue must be positive");
        }
    }

    /**
     * cronExpression 유효성 검증
     *
     * @param cronExpression 검증할 cron 표현식
     * @throws IllegalArgumentException cronExpression이 null이거나 빈 문자열인 경우
     */
    private static void validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("cronExpression must not be null or blank");
        }
    }

    public Long getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public Boolean isActive() {
        return active;
    }

    public CrawlIntervalType getIntervalType() {
        return intervalType;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public String getCronExpression() {
        return cronExpression;
    }
}
