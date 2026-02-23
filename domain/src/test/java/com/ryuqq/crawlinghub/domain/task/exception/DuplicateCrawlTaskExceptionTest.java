package com.ryuqq.crawlinghub.domain.task.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DuplicateCrawlTaskException 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DuplicateCrawlTaskException 테스트")
class DuplicateCrawlTaskExceptionTest {

    @Test
    @DisplayName("셀러 ID와 태스크 유형으로 예외 생성")
    void shouldCreateWithSellerIdAndTaskType() {
        // given
        Long sellerId = 12345L;
        CrawlTaskType taskType = CrawlTaskType.SEARCH;

        // when
        DuplicateCrawlTaskException exception = new DuplicateCrawlTaskException(sellerId, taskType);

        // then
        assertThat(exception.code()).isEqualTo("CRAWL-TASK-003");
        assertThat(exception.getMessage()).contains("중복");
        assertThat(exception.args()).containsEntry("sellerId", sellerId);
        assertThat(exception.args()).containsEntry("taskType", taskType.name());
    }
}
