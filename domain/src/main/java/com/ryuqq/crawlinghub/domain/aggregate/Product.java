package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.ItemNo;
import com.ryuqq.crawlinghub.domain.vo.ProductId;
import com.ryuqq.crawlinghub.domain.vo.SellerId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Product Aggregate Root
 *
 * <p>머스트잇에서 크롤링한 상품을 관리하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 Product 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 isComplete는 항상 false (INCOMPLETE)</li>
 *   <li>데이터 해시는 생성 시점에는 null (별도 업데이트 필요)</li>
 *   <li>isComplete()는 모든 해시 값이 존재할 때 true 반환</li>
 * </ul>
 */
public class Product {

    private final ProductId productId;
    private final ItemNo itemNo;
    private final SellerId sellerId;
    private String minishopDataHash;
    private String detailDataHash;
    private String optionDataHash;
    private Boolean isComplete;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     */
    private Product(ItemNo itemNo, SellerId sellerId) {
        this.productId = ProductId.generate();
        this.itemNo = itemNo;
        this.sellerId = sellerId;
        this.isComplete = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 Product 생성
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>초기 상태: INCOMPLETE (isComplete = false)</li>
     *   <li>데이터 해시: 모두 null (별도 업데이트 필요)</li>
     * </ul>
     *
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     * @return 새로 생성된 Product
     */
    public static Product create(ItemNo itemNo, SellerId sellerId) {
        return new Product(itemNo, sellerId);
    }

    /**
     * 미니샵 데이터 업데이트 및 해시 계산
     *
     * <p>변경 감지:</p>
     * <ul>
     *   <li>새 데이터의 MD5 해시와 기존 해시 비교</li>
     *   <li>변경 시 hasChanged = true, 동일 시 false</li>
     *   <li>업데이트 후 완료 상태 자동 갱신</li>
     * </ul>
     *
     * @param rawJson 미니샵 Raw JSON 데이터
     * @return 데이터 변경 시 true, 동일 시 false
     */
    public boolean updateMinishopData(String rawJson) {
        String newHash = calculateMD5Hash(rawJson);
        boolean hasChanged = !newHash.equals(minishopDataHash);
        this.minishopDataHash = newHash;
        updateCompleteStatus();
        this.updatedAt = LocalDateTime.now();
        return hasChanged;
    }

    /**
     * 상세 데이터 업데이트 및 해시 계산
     *
     * @param rawJson 상세 Raw JSON 데이터
     * @return 데이터 변경 시 true, 동일 시 false
     */
    public boolean updateDetailData(String rawJson) {
        String newHash = calculateMD5Hash(rawJson);
        boolean hasChanged = !newHash.equals(detailDataHash);
        this.detailDataHash = newHash;
        updateCompleteStatus();
        this.updatedAt = LocalDateTime.now();
        return hasChanged;
    }

    /**
     * 옵션 데이터 업데이트 및 해시 계산
     *
     * @param rawJson 옵션 Raw JSON 데이터
     * @return 데이터 변경 시 true, 동일 시 false
     */
    public boolean updateOptionData(String rawJson) {
        String newHash = calculateMD5Hash(rawJson);
        boolean hasChanged = !newHash.equals(optionDataHash);
        this.optionDataHash = newHash;
        updateCompleteStatus();
        this.updatedAt = LocalDateTime.now();
        return hasChanged;
    }

    /**
     * 완료 상태 업데이트
     *
     * <p>모든 데이터 해시가 존재하면 isComplete = true로 설정</p>
     */
    private void updateCompleteStatus() {
        this.isComplete = (minishopDataHash != null && detailDataHash != null && optionDataHash != null);
    }

    /**
     * MD5 해시 계산
     *
     * @param data 해시 계산할 데이터
     * @return MD5 해시 문자열 (32자 hex)
     * @throws RuntimeException MD5 알고리즘을 사용할 수 없는 경우
     */
    private String calculateMD5Hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 알고리즘을 사용할 수 없습니다", e);
        }
    }

    /**
     * 해시 값 변경 감지 (Tell Don't Ask 패턴)
     *
     * <p>정적 유틸리티 메서드로 해시 비교 로직을 캡슐화합니다.</p>
     *
     * <p>변경 감지 규칙:</p>
     * <ul>
     *   <li>둘 다 null → 변경 없음 (false)</li>
     *   <li>하나만 null → 변경 있음 (true)</li>
     *   <li>값이 다름 → 변경 있음 (true)</li>
     *   <li>값이 같음 → 변경 없음 (false)</li>
     * </ul>
     *
     * @param oldHash 이전 해시 값
     * @param newHash 새 해시 값
     * @return 변경 시 true, 동일 시 false
     */
    public static boolean hasChanged(String oldHash, String newHash) {
        if (oldHash == null && newHash == null) {
            return false;
        }
        if (oldHash == null || newHash == null) {
            return true;
        }
        return !oldHash.equals(newHash);
    }

    /**
     * 상품 데이터 완료 여부 확인 (Tell Don't Ask 패턴)
     *
     * <p>모든 데이터 해시가 존재할 때 true 반환:</p>
     * <ul>
     *   <li>minishopDataHash</li>
     *   <li>detailDataHash</li>
     *   <li>optionDataHash</li>
     * </ul>
     *
     * @return 완료 시 true, 불완전 시 false
     */
    public boolean isComplete() {
        return Boolean.TRUE.equals(isComplete);
    }

    // Getters (필요한 것만)
    public ProductId getProductId() {
        return productId;
    }

    public ItemNo getItemNo() {
        return itemNo;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public String getMinishopDataHash() {
        return minishopDataHash;
    }
}
