package com.ryuqq.crawlinghub.adapter.http.client.setof;

import com.ryuqq.crawlinghub.application.product.port.out.ExternalProductApiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SetOf 외부 Product API Adapter (Temporary Implementation)
 *
 * <p><strong>현재 상태: 임시 구현 (로깅만)</strong></p>
 * <ul>
 *   <li>실제 HTTP 호출 없음</li>
 *   <li>로깅만 수행 (동작 확인용)</li>
 *   <li>향후 실제 SetOf API 연동으로 대체 예정</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 준수:</strong></p>
 * <ul>
 *   <li>✅ @Transactional 내부에서 호출되지 않음</li>
 *   <li>✅ Scheduler/EventListener에서 트랜잭션 밖에서 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class SetOfAdapter implements ExternalProductApiPort {

    private static final Logger log = LoggerFactory.getLogger(SetOfAdapter.class);

    /**
     * 외부 Product API에 상품 정보 업데이트 (임시 구현: 로깅만)
     *
     * <p>⚠️ 현재는 로깅만 수행하며, 실제 HTTP 호출은 하지 않음
     *
     * @param productJson 상품 JSON 데이터
     */
    @Override
    public void updateProduct(String productJson) {
        log.info("🚀 [SetOfAdapter] External Product API 호출 (임시 로깅)");
        log.info("📦 Product JSON: {}", productJson);
        log.info("✅ [SetOfAdapter] External Product API 호출 완료 (임시 로깅)");

        // TODO: 향후 실제 SetOf API 연동 구현
        // - HTTP Client 설정 (RestTemplate 또는 WebClient)
        // - SetOf API 엔드포인트 호출
        // - 응답 처리 및 에러 핸들링
        // - Retry 정책 (Circuit Breaker 고려)
    }
}
