package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.messaging.ProductImagePayload;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.ProcessImageUploadFromSqsUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SQS 메시지 기반 이미지 업로드 처리 Service
 *
 * <p><strong>용도</strong>: SQS Listener에서 수신한 ProductImage 메시지를 처리
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Outbox 조회 및 상태 검증 (이미 처리된 경우 skip)
 *   <li>PROCESSING 상태로 변경
 *   <li>FileServerClient로 이미지 업로드 요청
 *   <li>성공 시: PROCESSING 상태 유지 (웹훅에서 COMPLETED 처리)
 *   <li>실패 시: FAILED 상태로 변경
 * </ol>
 *
 * <p><strong>멱등성</strong>: idempotencyKey를 통해 중복 처리 방지
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessImageUploadFromSqsService implements ProcessImageUploadFromSqsUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessImageUploadFromSqsService.class);

    private final ProductImageOutboxReadManager outboxReadManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final FileServerClient fileServerClient;

    public ProcessImageUploadFromSqsService(
            ProductImageOutboxReadManager outboxReadManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            CrawledProductImageReadManager imageReadManager,
            FileServerClient fileServerClient) {
        this.outboxReadManager = outboxReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.imageReadManager = imageReadManager;
        this.fileServerClient = fileServerClient;
    }

    @Override
    public boolean execute(ProductImagePayload payload) {
        log.debug(
                "SQS 이미지 업로드 처리 시작: outboxId={}, imageId={}",
                payload.outboxId(),
                payload.crawledProductImageId());

        // 1. Outbox 조회
        Optional<ProductImageOutbox> outboxOpt = outboxReadManager.findById(payload.outboxId());
        if (outboxOpt.isEmpty()) {
            log.warn("Outbox를 찾을 수 없음: outboxId={}", payload.outboxId());
            return false;
        }

        ProductImageOutbox outbox = outboxOpt.get();

        // 2. 상태 검증 (이미 처리 중이거나 완료된 경우 skip)
        if (outbox.getStatus().isProcessing() || outbox.isCompleted()) {
            log.debug(
                    "이미 처리 중이거나 완료됨 (skip): outboxId={}, status={}",
                    outbox.getId(),
                    outbox.getStatus());
            return true; // skip이지만 성공으로 처리 (ACK 필요)
        }

        // 3. PROCESSING 상태로 변경
        outboxTransactionManager.markAsProcessing(outbox);

        // 4. 이미지 정보 조회
        Optional<CrawledProductImage> imageOpt =
                imageReadManager.findById(outbox.getCrawledProductImageId());
        if (imageOpt.isEmpty()) {
            log.warn(
                    "이미지를 찾을 수 없음: outboxId={}, imageId={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId());
            outboxTransactionManager.markAsFailed(outbox, "이미지를 찾을 수 없음");
            return false;
        }

        CrawledProductImage image = imageOpt.get();

        // 5. FileServer API 호출
        try {
            FileServerClient.ImageUploadRequest request =
                    FileServerClient.ImageUploadRequest.of(
                            outbox.getIdempotencyKey(),
                            image.getOriginalUrl(),
                            image.getImageType().name());

            boolean requestSuccess = fileServerClient.requestImageUpload(request);

            if (requestSuccess) {
                log.info(
                        "SQS 이미지 업로드 요청 성공: outboxId={}, imageId={}",
                        outbox.getId(),
                        image.getId());
                return true;
            } else {
                outboxTransactionManager.markAsFailed(outbox, "FileServer 요청 실패");
                log.warn("SQS 이미지 업로드 요청 실패: outboxId={}", outbox.getId());
                return false;
            }
        } catch (Exception e) {
            outboxTransactionManager.markAsFailed(outbox, "FileServer 요청 예외: " + e.getMessage());
            log.error("SQS 이미지 업로드 처리 중 예외: outboxId={}, error={}", outbox.getId(), e.getMessage());
            return false;
        }
    }
}
