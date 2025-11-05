package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.assembler.command.ProductDetailCommand;
import com.ryuqq.crawlinghub.application.task.port.in.ProcessProductDetailUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.LoadCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.CrawlTaskId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 상세 크롤링 결과 처리 UseCase 구현체
 *
 * <p>상품 데이터를 저장하고 변경 감지 및 완성도 체크를 수행합니다.
 *
 * <p>⚠️ 주의:
 * <ul>
 *   <li>이 UseCase는 Product Aggregate와 연계됩니다</li>
 *   <li>Product Domain은 별도 bounded context로 관리됩니다</li>
 *   <li>현재는 태스크 완료 처리만 구현합니다</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class ProcessProductDetailService implements ProcessProductDetailUseCase {

    private final LoadCrawlTaskPort loadCrawlTaskPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;

    public ProcessProductDetailService(
        LoadCrawlTaskPort loadCrawlTaskPort,
        SaveCrawlTaskPort saveCrawlTaskPort
    ) {
        this.loadCrawlTaskPort = loadCrawlTaskPort;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
    }

    /**
     * 상품 상세 결과 처리
     *
     * <p>실행 순서:
     * 1. 태스크 조회
     * 2. 상품 조회/생성 (TODO: Product Aggregate 연동)
     * 3. 상세 데이터 저장
     * 4. 데이터 해시 계산
     * 5. 변경 감지 (이전 데이터와 비교)
     * 6. 완성도 체크 (필수 필드 검증)
     * 7. 태스크 완료 처리
     *
     * @param command 상품 상세 API 응답 데이터
     * @throws IllegalArgumentException 태스크를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void execute(ProductDetailCommand command) {
        CrawlTaskId taskId = CrawlTaskId.of(command.taskId());

        // 1. 태스크 조회
        CrawlTask task = loadCrawlTaskPort.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(
                "태스크를 찾을 수 없습니다: " + command.taskId()
            ));

        // 2. 상품 조회/생성
        // TODO: Product Aggregate 연동 필요
        // Product product = loadOrCreateProduct(command.itemNo());

        // 3. 상세 데이터 저장
        // TODO: JSON 파싱 및 Product 업데이트
        // updateProductDetail(product, command.responseData());

        // 4. 데이터 해시 계산
        // TODO: 데이터 해시 계산 유틸 구현
        // String dataHash = calculateDataHash(command.responseData());

        // 5. 변경 감지
        // TODO: 이전 데이터와 비교
        // boolean hasChanged = product.hasDataChanged(dataHash);

        // 6. 완성도 체크
        // TODO: 필수 필드 검증
        // boolean isComplete = product.isComplete();

        // 7. 태스크 완료 처리
        task.completeSuccessfully();
        saveCrawlTaskPort.save(task);

        // TODO: Product 저장
        // saveProductPort.save(product);

        // TODO: 변경 감지 시 이벤트 발행
        // if (hasChanged) {
        //     publishProductChangedEvent(product);
        // }
    }

    // TODO: Product Aggregate 연동 메서드들
    // - loadOrCreateProduct(String itemNo)
    // - updateProductDetail(Product product, String responseData)
    // - calculateDataHash(String responseData)
    // - publishProductChangedEvent(Product product)
}
