package com.ryuqq.crawlinghub.domain.change;

/**
 * ChangeData Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ChangeDataFixture {

    private static final String DEFAULT_DETAILS = "가격 변경: 10,000원 -> 9,000원";

    /**
     * 기본 ChangeData 생성
     *
     * @return ChangeData
     */
    public static ChangeData create() {
        return ChangeData.of(DEFAULT_DETAILS);
    }

    /**
     * 지정된 상세 정보로 ChangeData 생성
     *
     * @param details 변경 상세 정보
     * @return ChangeData
     */
    public static ChangeData createWithDetails(String details) {
        return ChangeData.of(details);
    }

    /**
     * 가격 변경 ChangeData 생성
     *
     * @return ChangeData
     */
    public static ChangeData createPriceChange() {
        return ChangeData.of("가격 변경: 50,000원 -> 45,000원");
    }

    /**
     * 재고 변경 ChangeData 생성
     *
     * @return ChangeData
     */
    public static ChangeData createStockChange() {
        return ChangeData.of("재고 변경: 100개 -> 50개");
    }

    /**
     * 옵션 변경 ChangeData 생성
     *
     * @return ChangeData
     */
    public static ChangeData createOptionChange() {
        return ChangeData.of("옵션 추가: 블랙, 화이트");
    }

    /**
     * 이미지 변경 ChangeData 생성
     *
     * @return ChangeData
     */
    public static ChangeData createImageChange() {
        return ChangeData.of("이미지 추가: image1.jpg, image2.jpg");
    }
}
