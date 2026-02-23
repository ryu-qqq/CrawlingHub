package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.internal.ProductSyncCoordinator;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessProductSyncFromSqsUseCase;
import org.springframework.stereotype.Service;

/**
 * SQS 메시지 기반 외부 서버 동기화 처리 Service
 *
 * <p>실제 흐름은 {@link ProductSyncCoordinator}에 위임합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessProductSyncFromSqsService implements ProcessProductSyncFromSqsUseCase {

    private final ProductSyncCoordinator productSyncCoordinator;

    public ProcessProductSyncFromSqsService(ProductSyncCoordinator productSyncCoordinator) {
        this.productSyncCoordinator = productSyncCoordinator;
    }

    @Override
    public boolean execute(ProcessProductSyncCommand command) {
        return productSyncCoordinator.processSyncRequest(command);
    }
}
