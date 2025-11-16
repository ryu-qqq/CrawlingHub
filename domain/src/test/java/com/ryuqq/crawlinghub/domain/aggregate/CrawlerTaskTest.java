package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.TaskId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlerTask Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - CrawlerTask 생성 (create) 테스트
 * - URL 검증 테스트
 * - 상태 전환 (publish, start) 테스트
 * - 완료/실패 (complete, fail) 테스트
 * - 재시도 로직 (retry) 테스트
 * - 리팩토링: 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 */
class CrawlerTaskTest {

    // ========== 리팩토링: 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateCrawlerTaskUsingForNew() {
        // Given
        SellerId sellerId = new SellerId("seller_new_001");
        CrawlerTaskType taskType = CrawlerTaskType.MINISHOP;
        String requestUrl = "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123";

        // When
        CrawlerTask task = CrawlerTask.forNew(sellerId, taskType, requestUrl);

        // Then
        assertThat(task.getTaskId()).isNotNull();
        assertThat(task.getSellerId()).isEqualTo(sellerId);
        assertThat(task.getTaskType()).isEqualTo(taskType);
        assertThat(task.getRequestUrl()).isEqualTo(requestUrl);
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.WAITING);
        assertThat(task.getRetryCount()).isEqualTo(0);
        assertThat(task.getErrorMessage()).isNull();
    }

    @Test
    void shouldCreateCrawlerTaskUsingOf() {
        // Given
        SellerId sellerId = new SellerId("seller_of_001");
        CrawlerTaskType taskType = CrawlerTaskType.PRODUCT_DETAIL;
        String requestUrl = "/mustit-api/facade-api/v1/item/12345/detail/top";

        // When
        CrawlerTask task = CrawlerTask.of(sellerId, taskType, requestUrl);

        // Then
        assertThat(task.getTaskId()).isNotNull();
        assertThat(task.getSellerId()).isEqualTo(sellerId);
        assertThat(task.getTaskType()).isEqualTo(taskType);
        assertThat(task.getRequestUrl()).isEqualTo(requestUrl);
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.WAITING);
    }

    @Test
    void shouldReconstituteCrawlerTaskWithAllFields() {
        // Given
        TaskId taskId = TaskId.generate();
        SellerId sellerId = new SellerId("seller_recon_001");
        CrawlerTaskType taskType = CrawlerTaskType.PRODUCT_OPTION;
        String requestUrl = "/mustit-api/facade-api/v1/auction_products/67890/options";
        CrawlerTaskStatus status = CrawlerTaskStatus.FAILED;
        Integer retryCount = 1;
        String errorMessage = "Network timeout";

        // When
        CrawlerTask task = CrawlerTask.reconstitute(taskId, sellerId, taskType, requestUrl,
                                                      status, retryCount, errorMessage);

        // Then
        assertThat(task.getTaskId()).isEqualTo(taskId);
        assertThat(task.getSellerId()).isEqualTo(sellerId);
        assertThat(task.getTaskType()).isEqualTo(taskType);
        assertThat(task.getRequestUrl()).isEqualTo(requestUrl);
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.FAILED);
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getErrorMessage()).isEqualTo("Network timeout");
    }

    // ========== 기존 테스트 (레거시, 유지보수용) ==========


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

    @Test
    void shouldPublishTaskFromWaiting() {
        // Given - WAITING 상태의 task
        SellerId sellerId = new SellerId("seller_003");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );

        // When
        task.publish();

        // Then
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.PUBLISHED);
    }

    @Test
    void shouldStartTaskFromPublished() {
        // Given - PUBLISHED 상태의 task (WAITING → PUBLISHED)
        SellerId sellerId = new SellerId("seller_004");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish(); // WAITING → PUBLISHED

        // When
        task.start();

        // Then
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowExceptionWhenPublishNonWaitingTask() {
        // Given - PUBLISHED 상태의 task
        SellerId sellerId = new SellerId("seller_005");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish(); // WAITING → PUBLISHED

        // When & Then
        assertThatThrownBy(() -> task.publish())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("WAITING 상태에서만 발행할 수 있습니다");
    }

    @Test
    void shouldCompleteTaskFromInProgress() {
        // Given - IN_PROGRESS 상태의 task (WAITING → PUBLISHED → IN_PROGRESS)
        SellerId sellerId = new SellerId("seller_006");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish(); // WAITING → PUBLISHED
        task.start();   // PUBLISHED → IN_PROGRESS

        // When
        task.complete();

        // Then
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.COMPLETED);
    }

    @Test
    void shouldFailTaskWithErrorMessage() {
        // Given - IN_PROGRESS 상태의 task
        SellerId sellerId = new SellerId("seller_007");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish();
        task.start();
        String errorMessage = "429 Too Many Requests";

        // When
        task.fail(errorMessage);

        // Then
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void shouldRetryWhenRetryCountLessThan2() {
        // Given - FAILED 상태의 task (retryCount = 0)
        SellerId sellerId = new SellerId("seller_008");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish();
        task.start();
        task.fail("Network error");

        // When
        task.retry();

        // Then
        assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.RETRY);
        assertThat(task.getRetryCount()).isEqualTo(1);
    }

    @Test
    void shouldNotRetryWhenRetryCountExceeds2() {
        // Given - retryCount = 2인 FAILED task
        // 수동으로 retryCount를 2로 설정해야 함 (현재 TestFixture 없음)
        SellerId sellerId = new SellerId("seller_009");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish();
        task.start();
        // 첫 번째 재시도
        task.fail("Network error");
        task.retry(); // retryCount = 1
        // 두 번째 재시도
        task.start(); // RETRY → IN_PROGRESS (필요 시 구현)
        task.fail("Network error");
        task.retry(); // retryCount = 2

        // 세 번째 재시도 시도
        task.start();
        task.fail("Network error");

        // When & Then
        assertThatThrownBy(() -> task.retry())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("재시도 횟수를 초과했습니다 (최대 2회)");
    }

    @Test
    void shouldResetErrorMessageOnRetry() {
        // Given - FAILED 상태의 task
        SellerId sellerId = new SellerId("seller_010");
        CrawlerTask task = CrawlerTask.create(
            sellerId,
            CrawlerTaskType.MINISHOP,
            "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123"
        );
        task.publish();
        task.start();
        task.fail("Network error");

        // When
        task.retry();

        // Then
        assertThat(task.getErrorMessage()).isNull();
    }
}
