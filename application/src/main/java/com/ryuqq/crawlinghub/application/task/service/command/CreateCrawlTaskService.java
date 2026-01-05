package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.port.in.command.CreateCrawlTaskUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 동적 생성 Service
 *
 * <p>CreateCrawlTaskUseCase 구현체
 *
 * <p><strong>용도</strong>:
 *
 * <ul>
 *   <li>크롤러가 크롤링 결과에서 후속 태스크를 동적으로 생성
 *   <li>MetaCrawler → MINI_SHOP 태스크 생성
 *   <li>MiniShopCrawler → DETAIL, OPTION 태스크 생성
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>CommandFactory로 CrawlTaskBundle 생성 (Task + Outbox payload)
 *   <li>Facade에서 persist (중복 검증 + 저장 + 이벤트 발행)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CreateCrawlTaskService implements CreateCrawlTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCrawlTaskService.class);

    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskFacade facade;

    public CreateCrawlTaskService(CrawlTaskCommandFactory commandFactory, CrawlTaskFacade facade) {
        this.commandFactory = commandFactory;
        this.facade = facade;
    }

    @Override
    public void execute(CreateCrawlTaskCommand command) {
        log.info(
                "CrawlTask 동적 생성: schedulerId={}, sellerId={}, taskType={}, targetId={}",
                command.crawlSchedulerId(),
                command.sellerId(),
                command.taskType(),
                command.targetId());

        // 1. CrawlTaskBundle 생성 (CommandFactory)
        CrawlTaskBundle bundle = commandFactory.createBundle(command);

        // 2. Facade에서 persist (중복 검증 + 저장 + 이벤트 발행)
        facade.persist(bundle);

        log.info("CrawlTask 동적 생성 완료: taskType={}", command.taskType());
    }

    @Override
    public void executeBatch(List<CreateCrawlTaskCommand> commands) {
        log.info("CrawlTask 일괄 동적 생성: count={}", commands.size());

        int successCount = 0;
        int failCount = 0;

        for (CreateCrawlTaskCommand command : commands) {
            try {
                execute(command);
                successCount++;
            } catch (RuntimeException e) {
                failCount++;
                // 상세 예외 정보 로깅 (디버깅용)
                log.error(
                        "🚨 CrawlTask 생성 실패 - 예외 발생! "
                                + "exceptionClass={}, message={}, "
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
                "CrawlTask 일괄 동적 생성 완료: total={}, success={}, fail={}",
                commands.size(),
                successCount,
                failCount);
    }
}
