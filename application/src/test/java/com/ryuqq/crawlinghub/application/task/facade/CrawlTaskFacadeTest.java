package com.ryuqq.crawlinghub.application.task.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * CrawlTaskFacade 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Validator, TransactionManager, EventPublisher Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskFacade 테스트")
class CrawlTaskFacadeTest {

    @Mock private CrawlTaskPersistenceValidator validator;

    @Mock private CrawlTaskTransactionManager transactionManager;

    @Mock private CrawlTaskOutboxTransactionManager outboxTransactionManager;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private CrawlTaskFacade facade;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlTaskBundle 저장 및 이벤트 발행")
        void shouldPersistBundleAndPublishEvents() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskBundle bundle = CrawlTaskBundle.of(task, "{\"payload\": \"test\"}");
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(transactionManager.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskBundle result = facade.persist(bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSavedTaskId()).isEqualTo(expectedId);
            verify(validator)
                    .validateNoDuplicateTask(
                            bundle.getCrawlScheduleId(), task.getSellerId(), task.getTaskType());
            verify(transactionManager).persist(task);
            verify(outboxTransactionManager).persist(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[성공] 중복 검증 후 저장 진행")
        void shouldValidateBeforePersist() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskBundle bundle = CrawlTaskBundle.of(task, "{\"payload\": \"test\"}");
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(transactionManager.persist(task)).willReturn(expectedId);

            // When
            facade.persist(bundle);

            // Then
            verify(validator)
                    .validateNoDuplicateTask(
                            bundle.getCrawlScheduleId(), task.getSellerId(), task.getTaskType());
        }
    }
}
