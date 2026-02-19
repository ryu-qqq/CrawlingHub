package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * SearchField - 검색 필드 마커 인터페이스
 *
 * <p>각 Bounded Context에서 검색 가능한 필드를 enum으로 정의할 때 구현합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>각 BC는 자신만의 SearchField enum을 정의
 *   <li>검색 가능한 필드만 enum 값으로 노출
 *   <li>DB 컬럼명은 Adapter에서 매핑 (도메인 언어 유지)
 * </ul>
 *
 * <p><strong>구현 예시:</strong>
 *
 * <pre>{@code
 * public enum LayerSearchField implements SearchField {
 *     CODE("code"), NAME("name"), DESCRIPTION("description");
 *
 *     private final String fieldName;
 *
 *     LayerSearchField(String fieldName) {
 *         this.fieldName = fieldName;
 *     }
 *
 *     @Override
 *     public String fieldName() {
 *         return fieldName;
 *     }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchField {

    /**
     * 검색 필드명 반환
     *
     * <p>도메인 언어로 표현된 필드명입니다. 실제 DB 컬럼명으로의 매핑은 Adapter 레이어에서 수행합니다.
     *
     * @return 필드명 (예: "code", "name", "description")
     */
    String fieldName();

    /**
     * enum 이름 반환 (기본 구현)
     *
     * @return enum 상수 이름
     */
    default String name() {
        return this.toString();
    }
}
