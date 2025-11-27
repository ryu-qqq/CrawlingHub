package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
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
 *   <li>Assembler로 CrawlTaskBundle 생성 (Task + Outbox payload)
 *   <li>Facade에서 persist (중복 검증 + 저장 + 이벤트 발행)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CreateCrawlTaskService implements CreateCrawlTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCrawlTaskService.class);

    private final CrawlTaskAssembler assembler;
    private final CrawlTaskFacade facade;

    public CreateCrawlTaskService(CrawlTaskAssembler assembler, CrawlTaskFacade facade) {
        this.assembler = assembler;
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

        // 1. CrawlTaskBundle 생성 (Assembler)
        CrawlTaskBundle bundle = assembler.toBundle(command);

        // 2. Facade에서 persist (중복 검증 + 저장 + 이벤트 발행)
        facade.persist(bundle);

        log.info("CrawlTask 동적 생성 완료: taskType={}", command.taskType());
    }

    @Override
    public void executeBatch(List<CreateCrawlTaskCommand> commands) {
        log.info("CrawlTask 일괄 동적 생성: count={}", commands.size());

        for (CreateCrawlTaskCommand command : commands) {
            try {
                execute(command);
            } catch (RuntimeException e) {
                // 개별 실패 시에도 나머지는 계속 처리
                log.warn(
                        "CrawlTask 생성 실패 (계속 진행): taskType={}, targetId={}, error={}",
                        command.taskType(),
                        command.targetId(),
                        e.getMessage());
            }
        }

        log.info("CrawlTask 일괄 동적 생성 완료");
    }
}
