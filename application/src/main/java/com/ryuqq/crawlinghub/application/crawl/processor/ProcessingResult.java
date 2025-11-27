package com.ryuqq.crawlinghub.application.crawl.processor;

import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import java.util.Collections;
import java.util.List;

/**
 * 크롤링 결과 처리 결과 DTO
 *
 * <p>CrawlResultProcessor의 처리 결과를 담는 DTO. 후속 CrawlTask 생성 커맨드 목록과 처리 통계를 포함.
 *
 * <p><strong>포함 정보</strong>:
 *
 * <ul>
 *   <li>후속 Task 생성 커맨드 목록
 *   <li>파싱된 아이템 수
 *   <li>저장된 아이템 수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProcessingResult {

    private final List<CreateCrawlTaskCommand> followUpCommands;
    private final int parsedItemCount;
    private final int savedItemCount;

    private ProcessingResult(
            List<CreateCrawlTaskCommand> followUpCommands, int parsedItemCount, int savedItemCount) {
        this.followUpCommands =
                followUpCommands == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(followUpCommands);
        this.parsedItemCount = parsedItemCount;
        this.savedItemCount = savedItemCount;
    }

    /**
     * 후속 Task가 있는 처리 결과 생성
     *
     * @param followUpCommands 후속 Task 생성 커맨드 목록
     * @param parsedItemCount 파싱된 아이템 수
     * @param savedItemCount 저장된 아이템 수
     * @return ProcessingResult
     */
    public static ProcessingResult withFollowUp(
            List<CreateCrawlTaskCommand> followUpCommands,
            int parsedItemCount,
            int savedItemCount) {
        return new ProcessingResult(followUpCommands, parsedItemCount, savedItemCount);
    }

    /**
     * 후속 Task 없이 처리 완료 결과 생성
     *
     * @param parsedItemCount 파싱된 아이템 수
     * @param savedItemCount 저장된 아이템 수
     * @return ProcessingResult
     */
    public static ProcessingResult completed(int parsedItemCount, int savedItemCount) {
        return new ProcessingResult(Collections.emptyList(), parsedItemCount, savedItemCount);
    }

    /**
     * 빈 처리 결과 생성 (파싱 결과 없음)
     *
     * @return ProcessingResult
     */
    public static ProcessingResult empty() {
        return new ProcessingResult(Collections.emptyList(), 0, 0);
    }

    /**
     * 후속 Task 존재 여부 확인
     *
     * @return 후속 Task가 있으면 true
     */
    public boolean hasFollowUpTasks() {
        return !followUpCommands.isEmpty();
    }

    public List<CreateCrawlTaskCommand> getFollowUpCommands() {
        return followUpCommands;
    }

    public int getParsedItemCount() {
        return parsedItemCount;
    }

    public int getSavedItemCount() {
        return savedItemCount;
    }
}
