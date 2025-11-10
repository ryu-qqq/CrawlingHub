package com.ryuqq.crawlinghub.domain.task.input;

/**
 * Task Input Parameter 추상 클래스
 *
 * <p>모든 TaskType별 InputParam의 부모 클래스
 *
 * <p>타입 안전성을 위한 마커 클래스로, 각 Task 타입별로 구체적인 파라미터 클래스를 구현합니다:
 * <ul>
 *   <li>{@link MetaTaskInputParam} - META 태스크용 (sellerId, pageNo=0, pageSize=1)</li>
 *   <li>{@link MiniShopTaskInputParam} - MINI_SHOP 태스크용 (sellerId, pageNo, pageSize=500)</li>
 *   <li>{@link ProductDetailTaskInputParam} - PRODUCT_DETAIL 태스크용 (itemNo)</li>
 *   <li>{@link ProductOptionTaskInputParam} - PRODUCT_OPTION 태스크용 (itemNo)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public abstract class TaskInputParam {

    /**
     * Protected 생성자
     *
     * <p>하위 클래스에서만 인스턴스 생성 가능
     */
    protected TaskInputParam() {
    }
}
