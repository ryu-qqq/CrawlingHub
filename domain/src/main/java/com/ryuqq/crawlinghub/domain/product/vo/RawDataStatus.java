package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * Raw 데이터 처리 상태
 *
 * @author development-team
 * @since 1.0.0
 */
public enum RawDataStatus {

    /** 대기 중 - 가공 파이프라인 처리 대기 */
    PENDING,

    /** 처리 완료 - 가공 파이프라인 처리 완료 */
    PROCESSED,

    /** 처리 실패 - 가공 중 오류 발생 */
    FAILED
}
