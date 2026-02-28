package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 마켓플레이스 고시정보 필드 코드
 *
 * <p>상품정보제공고시에 사용되는 필드 코드 목록입니다.
 */
public enum NoticeFieldCode {
    MATERIAL("소재"),
    COLOR("색상"),
    SIZE("사이즈"),
    MANUFACTURER("제조사/수입자"),
    MADE_IN("원산지"),
    WASH_CARE("세탁방법"),
    RELEASE_DATE("출시일"),
    QUALITY_ASSURANCE("품질보증기준"),
    MATERIAL_UPPER("갑피 소재"),
    MATERIAL_SOLE("밑창 소재"),
    TYPE("종류"),
    CARE_INSTRUCTIONS("취급시 주의사항"),
    CAPACITY("용량"),
    PRODUCT_TYPE("제품 유형"),
    USAGE("사용법/용도"),
    HOW_TO_USE("사용방법"),
    INGREDIENTS("성분"),
    FUNCTIONAL_COSMETIC("기능성 화장품"),
    CAUTION("주의사항"),
    CS_INFO("소비자상담 연락처"),
    WEIGHT("중량"),
    GEMSTONE_INFO("보석 정보"),
    CERTIFICATION("인증정보"),
    MOVEMENT("무브먼트"),
    CASE_MATERIAL("케이스 소재"),
    BAND_MATERIAL("밴드 소재"),
    WATER_RESISTANCE("방수"),
    DELIVERY("배송정보"),
    AGE_RANGE("사용연령"),
    MODEL("모델명"),
    RATED_VOLTAGE("정격전압"),
    ENERGY_RATING("에너지소비효율등급"),
    PRODUCT_NAME("상품명");

    private final String displayName;

    NoticeFieldCode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String code() {
        return name();
    }
}
