package com.ryuqq.crawlinghub.domain.change;

import com.ryuqq.crawlinghub.domain.product.ProductId;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ChangeDetection Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ChangeDetectionFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final Long DEFAULT_PRODUCT_ID = 100L;
    private static final ChangeType DEFAULT_CHANGE_TYPE = ChangeType.PRICE;
    private static final String DEFAULT_PREVIOUS_HASH = "abc123";
    private static final String DEFAULT_CURRENT_HASH = "def456";
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 ChangeDetection 생성 (신규)
     *
     * @return ChangeDetection
     */
    public static ChangeDetection create() {
        return ChangeDetection.forNew(
            ProductId.of(DEFAULT_PRODUCT_ID),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create()
        );
    }

    /**
     * ID를 가진 ChangeDetection 생성
     *
     * @param id ChangeDetection ID
     * @return ChangeDetection
     */
    public static ChangeDetection createWithId(Long id) {
        return ChangeDetection.of(
            ChangeDetectionId.of(id),
            ProductId.of(DEFAULT_PRODUCT_ID),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create()
        );
    }

    /**
     * 특정 상품 ID로 ChangeDetection 생성
     *
     * @param productId 상품 ID
     * @return ChangeDetection
     */
    public static ChangeDetection createWithProductId(Long productId) {
        return ChangeDetection.forNew(
            ProductId.of(productId),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create()
        );
    }

    /**
     * 특정 변경 유형으로 ChangeDetection 생성
     *
     * @param changeType 변경 유형
     * @return ChangeDetection
     */
    public static ChangeDetection createWithChangeType(ChangeType changeType) {
        ChangeData changeData = switch (changeType) {
            case PRICE -> ChangeDataFixture.createPriceChange();
            case STOCK -> ChangeDataFixture.createStockChange();
            case OPTION -> ChangeDataFixture.createOptionChange();
            case IMAGE -> ChangeDataFixture.createImageChange();
        };

        return ChangeDetection.forNew(
            ProductId.of(DEFAULT_PRODUCT_ID),
            changeType,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            changeData
        );
    }

    /**
     * DB reconstitute용 ChangeDetection 생성
     *
     * @param id ChangeDetection ID
     * @param productId 상품 ID
     * @param status 알림 상태
     * @return ChangeDetection
     */
    public static ChangeDetection reconstitute(
        Long id,
        Long productId,
        NotificationStatus status
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ChangeDetection.reconstitute(
            ChangeDetectionId.of(id),
            ProductId.of(productId),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create(),
            status,
            0,
            null,
            now,
            null,
            now,
            now
        );
    }

    /**
     * PENDING 상태의 ChangeDetection 생성
     *
     * @return ChangeDetection
     */
    public static ChangeDetection createPending() {
        return reconstitute(DEFAULT_ID, DEFAULT_PRODUCT_ID, NotificationStatus.PENDING);
    }

    /**
     * SENT 상태의 ChangeDetection 생성
     *
     * @return ChangeDetection
     */
    public static ChangeDetection createSent() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ChangeDetection.reconstitute(
            ChangeDetectionId.of(DEFAULT_ID),
            ProductId.of(DEFAULT_PRODUCT_ID),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create(),
            NotificationStatus.SENT,
            0,
            null,
            now,
            now,
            now,
            now
        );
    }

    /**
     * FAILED 상태의 ChangeDetection 생성
     *
     * @param retryCount 재시도 횟수
     * @return ChangeDetection
     */
    public static ChangeDetection createFailed(int retryCount) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ChangeDetection.reconstitute(
            ChangeDetectionId.of(DEFAULT_ID),
            ProductId.of(DEFAULT_PRODUCT_ID),
            DEFAULT_CHANGE_TYPE,
            DEFAULT_PREVIOUS_HASH,
            DEFAULT_CURRENT_HASH,
            ChangeDataFixture.create(),
            NotificationStatus.FAILED,
            retryCount,
            "전송 실패: 네트워크 오류",
            now,
            null,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 ChangeDetection 생성
     *
     * @param id ChangeDetection ID
     * @param productId 상품 ID
     * @param changeType 변경 유형
     * @param previousHash 이전 해시
     * @param currentHash 현재 해시
     * @param changeData 변경 상세 정보
     * @return ChangeDetection
     */
    public static ChangeDetection createCustom(
        Long id,
        Long productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeData
    ) {
        if (id == null) {
            return ChangeDetection.forNew(
                ProductId.of(productId),
                changeType,
                previousHash,
                currentHash,
                changeData
            );
        }
        return ChangeDetection.of(
            ChangeDetectionId.of(id),
            ProductId.of(productId),
            changeType,
            previousHash,
            currentHash,
            changeData
        );
    }
}
