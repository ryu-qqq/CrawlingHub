package com.ryuqq.crawlinghub.application.task.assembler.command;

/**
 * 상품 상세 크롤링 결과 처리 Command
 *
 * @param taskId       태스크 ID (필수)
 * @param itemNo       상품 번호 (필수)
 * @param responseData 상품 상세 API 응답 데이터 (JSON) (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record ProductDetailCommand(
    Long taskId,
    String itemNo,
    String responseData
) {
    public ProductDetailCommand {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID는 필수입니다");
        }
        if (itemNo == null || itemNo.isBlank()) {
            throw new IllegalArgumentException("상품 번호는 필수입니다");
        }
        if (responseData == null || responseData.isBlank()) {
            throw new IllegalArgumentException("응답 데이터는 필수입니다");
        }
    }
}
