package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageOutboxTimeoutResult;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageOutboxTimeoutUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이미지 Outbox 타임아웃 처리 Service
 *
 * <p>PROCESSING 상태로 장시간 머물러 있는 Outbox를 FAILED로 변경하여 재시도 가능하게 합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>타임아웃된 PROCESSING Outbox 조회
 *   <li>각 Outbox를 FAILED 상태로 변경 (타임아웃 에러 메시지 포함)
 *   <li>변경된 Outbox 저장
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class HandleImageOutboxTimeoutService implements HandleImageOutboxTimeoutUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(HandleImageOutboxTimeoutService.class);

    /** 타임아웃 기준 시간: 10분 (600초) */
    private static final int TIMEOUT_SECONDS = 600;

    /** 한 번에 처리할 최대 개수 */
    private static final int BATCH_SIZE = 100;

    private static final String TIMEOUT_ERROR_MESSAGE = "PROCESSING 상태 타임아웃 (10분 초과)";

    private final ImageOutboxQueryPort imageOutboxQueryPort;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final Clock clock;

    public HandleImageOutboxTimeoutService(
            ImageOutboxQueryPort imageOutboxQueryPort,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            Clock clock) {
        this.imageOutboxQueryPort = imageOutboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ImageOutboxTimeoutResult execute() {
        List<ProductImageOutbox> timedOutOutboxes =
                imageOutboxQueryPort.findTimedOutProcessingOutboxes(TIMEOUT_SECONDS, BATCH_SIZE);

        if (timedOutOutboxes.isEmpty()) {
            return ImageOutboxTimeoutResult.empty();
        }

        log.info("타임아웃된 ImageOutbox {} 건 발견", timedOutOutboxes.size());

        for (ProductImageOutbox outbox : timedOutOutboxes) {
            outbox.markAsFailed(TIMEOUT_ERROR_MESSAGE, clock);
            outboxTransactionManager.persist(outbox);

            log.debug(
                    "ImageOutbox ID={} 타임아웃 처리 완료 (imageId={})",
                    outbox.getId(),
                    outbox.getCrawledProductImageId());
        }

        boolean hasMore = timedOutOutboxes.size() >= BATCH_SIZE;
        return ImageOutboxTimeoutResult.of(timedOutOutboxes.size(), hasMore);
    }
}
