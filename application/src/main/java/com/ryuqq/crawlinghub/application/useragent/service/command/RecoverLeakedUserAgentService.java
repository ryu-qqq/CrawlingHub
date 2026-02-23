package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverLeakedUserAgentUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Leak된 UserAgent 복구 서비스
 *
 * <p>BORROWED 상태로 지정 시간 이상 머문 UserAgent를 감지하여 강제 반납합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverLeakedUserAgentService implements RecoverLeakedUserAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoverLeakedUserAgentService.class);

    private final UserAgentPoolCacheCommandManager cacheCommandManager;

    public RecoverLeakedUserAgentService(UserAgentPoolCacheCommandManager cacheCommandManager) {
        this.cacheCommandManager = cacheCommandManager;
    }

    @Override
    public int execute(RecoverLeakedUserAgentCommand command) {
        List<Long> leakedIds =
                cacheCommandManager.detectLeakedAgents(command.leakThresholdMillis());
        if (leakedIds.isEmpty()) {
            return 0;
        }

        log.warn("Leak 감지: {}건 BORROWED 상태 초과", leakedIds.size());
        int recoveredCount = 0;

        for (Long id : leakedIds) {
            try {
                cacheCommandManager.returnAgent(id, false, 0, 0, null, 0);
                log.warn("Leaked UserAgent {} 강제 반납", id);
                recoveredCount++;
            } catch (Exception e) {
                log.error("Leaked UserAgent {} 강제 반납 실패", id, e);
            }
        }

        return recoveredCount;
    }
}
