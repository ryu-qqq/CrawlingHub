package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlerTask Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - CrawlerTask 생성 (create) 테스트
 * - URL 검증 테스트
 */
class CrawlerTaskTest {

    @Test
    void shouldCreateCrawlerTaskWithWaitingStatus() {
        // Given
        SellerId sellerId = new SellerId("seller_001");
        CrawlerTaskType taskType = CrawlerTaskType.MINISHOP;
        String requestUrl = "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123";

        // When
        CrawlerTask task = CrawlerTask.create(sellerId, taskType, requestUrl);

        // Then
        assertThat(task.getTaskId()).isNotNull();
        assertThat(task.getSellerId()).isEqualTo(sellerId);
        assertThat(task.getTaskType()).isEqualTo(taskType);
        assertThat(task.getRequestUrl()).isEqualTo(requestUrl);
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.WAITING);
        assertThat(task.getRetryCount()).isEqualTo(0);
    }

    @Test
    void shouldValidateMinishopUrlFormat() {
        // Given
        SellerId sellerId = new SellerId("seller_002");
        CrawlerTaskType taskType = CrawlerTaskType.MINISHOP;
        String invalidUrl = "/invalid-url";

        // When & Then
        assertThatThrownBy(() -> CrawlerTask.create(sellerId, taskType, invalidUrl))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MINISHOP URL 형식이 올바르지 않습니다");
    }
}
