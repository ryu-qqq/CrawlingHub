package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import java.util.List;

/**
 * CrawlTask 동적 생성 UseCase (Port In - Command)
 *
 * <p>크롤러가 크롤링 결과에서 후속 태스크를 동적으로 생성할 때 사용. TriggerCrawlTaskUseCase와 다르게, 스케줄러가 아닌 크롤러가 호출.
 *
 * <p><strong>크롤링 플로우</strong>:
 *
 * <pre>
 * TriggerCrawlTaskUseCase (스케줄러 트리거)
 *     → META 태스크 생성 → SQS
 *     → MetaCrawler 실행
 *     → CreateCrawlTaskUseCase (동적 생성)
 *         → MINI_SHOP 태스크들 생성 → SQS
 *         → MiniShopCrawler 실행
 *         → CreateCrawlTaskUseCase (동적 생성)
 *             → DETAIL, OPTION 태스크들 생성 → SQS
 * </pre>
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>태스크 저장 및 Outbox 생성: 트랜잭션 내
 *   <li>SQS 발행: 트랜잭션 외부 (Outbox 패턴)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CreateCrawlTaskUseCase {

    /**
     * 단일 CrawlTask 생성 및 SQS 발행
     *
     * @param command 생성 커맨드
     */
    void execute(CreateCrawlTaskCommand command);

    /**
     * 복수 CrawlTask 일괄 생성 및 SQS 발행
     *
     * <p>크롤링 결과에서 여러 후속 태스크를 한 번에 생성할 때 사용. 예: MiniShopCrawler가 상품 목록에서 DETAIL + OPTION 태스크를 일괄 생성
     *
     * @param commands 생성 커맨드 목록
     */
    void executeBatch(List<CreateCrawlTaskCommand> commands);
}
