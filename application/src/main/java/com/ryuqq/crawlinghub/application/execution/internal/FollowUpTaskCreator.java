package com.ryuqq.crawlinghub.application.execution.internal;

import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.validator.CrawlTaskPersistenceValidator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 후속 CrawlTask 생성기
 *
 * <p>크롤링 결과에서 발견된 후속 태스크를 생성합니다. execution 패키지 내부에서 task 패키지의 컴포넌트를 직접 사용하여 UseCase 역방향 의존성을 제거합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FollowUpTaskCreator {

    private static final Logger log = LoggerFactory.getLogger(FollowUpTaskCreator.class);

    private final CrawlTaskPersistenceValidator validator;
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskCommandFacade coordinator;

    public FollowUpTaskCreator(
            CrawlTaskPersistenceValidator validator,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskCommandFacade coordinator) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.coordinator = coordinator;
    }

    /**
     * 단일 CrawlTask 생성
     *
     * @param command 생성 커맨드
     */
    public void execute(CreateCrawlTaskCommand command) {
        log.info(
                "후속 CrawlTask 생성: schedulerId={}, sellerId={}, taskType={}, targetId={}",
                command.crawlSchedulerId(),
                command.sellerId(),
                command.taskType(),
                command.targetId());

        CrawlTaskBundle bundle = commandFactory.createBundle(command);
        validator.validateNoDuplicateTask(bundle.crawlTask());
        coordinator.persist(bundle);

        log.info("후속 CrawlTask 생성 완료: taskType={}", command.taskType());
    }

    /**
     * 복수 CrawlTask 일괄 생성
     *
     * @param commands 생성 커맨드 목록
     */
    public void executeBatch(List<CreateCrawlTaskCommand> commands) {
        log.info("후속 CrawlTask 일괄 생성: count={}", commands.size());

        int successCount = 0;
        int failCount = 0;

        for (CreateCrawlTaskCommand command : commands) {
            try {
                execute(command);
                successCount++;
            } catch (RuntimeException e) {
                failCount++;
                log.error(
                        "후속 CrawlTask 생성 실패: exceptionClass={}, message={}, "
                                + "schedulerId={}, sellerId={}, taskType={}, targetId={}",
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        command.crawlSchedulerId(),
                        command.sellerId(),
                        command.taskType(),
                        command.targetId(),
                        e);
            }
        }

        log.info(
                "후속 CrawlTask 일괄 생성 완료: total={}, success={}, fail={}",
                commands.size(),
                successCount,
                failCount);
    }
}
