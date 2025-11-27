package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecordUserAgentResultUseCase;
import org.springframework.stereotype.Service;

/**
 * UserAgent 결과 기록 Service
 *
 * <p>{@link RecordUserAgentResultUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecordUserAgentResultService implements RecordUserAgentResultUseCase {

    private final UserAgentPoolManager poolManager;

    public RecordUserAgentResultService(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public void execute(RecordUserAgentResultCommand command) {
        poolManager.recordResult(command);
    }
}
