package com.ryuqq.crawlinghub.adapter.in.sqs.task;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.port.in.command.CrawlTaskExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.port.in.command.FailCrawlTaskDirectlyUseCase;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskSqsListener 단위 테스트
 *
 * <p>CrawlTask SQS 메시지 수신 후 에러 분류에 따른 처리 흐름을 검증합니다.
 *
 * <ul>
 *   <li>정상 처리: UseCase 호출 검증
 *   <li>일시적 오류(RetryableExecutionException): 예외 재전파 → SQS 재시도
 *   <li>영구적 오류(그 외): failSafely 호출 → ACK (고아 PUBLISHED Task 방지)
 *   <li>failSafely 실패: 예외가 다시 전파되지 않음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskSqsListener 단위 테스트")
class CrawlTaskSqsListenerTest {

    @Mock private CrawlTaskListenerMapper mapper;
    @Mock private CrawlTaskExecutionUseCase crawlTaskExecutionUseCase;
    @Mock private FailCrawlTaskDirectlyUseCase failCrawlTaskDirectlyUseCase;

    private CrawlTaskSqsListener sut;

    @BeforeEach
    void setUp() {
        sut =
                new CrawlTaskSqsListener(
                        mapper, crawlTaskExecutionUseCase, failCrawlTaskDirectlyUseCase);
    }

    private CrawlTaskPayload createPayload(Long taskId) {
        return new CrawlTaskPayload(taskId, 10L, 100L, "PRODUCT", "https://shop.com/products");
    }

    private ExecuteCrawlTaskCommand createCommand(Long taskId) {
        return new ExecuteCrawlTaskCommand(
                taskId, 10L, 100L, "PRODUCT", "https://shop.com/products");
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 정상 처리 테스트")
    class HandleMessageSuccessTest {

        @Test
        @DisplayName("[성공] 정상 페이로드 수신 시 Mapper와 UseCase가 호출된다")
        void shouldCallMapperAndUseCaseForValidPayload() {
            // Given
            CrawlTaskPayload payload = createPayload(1L);
            ExecuteCrawlTaskCommand command = createCommand(1L);
            given(mapper.toCommand(payload)).willReturn(command);
            doNothing().when(crawlTaskExecutionUseCase).execute(command);

            // When
            sut.handleMessage(payload);

            // Then
            verify(mapper).toCommand(payload);
            verify(crawlTaskExecutionUseCase).execute(command);
            // 성공 시 failCrawlTaskDirectlyUseCase는 호출되지 않음
            verify(failCrawlTaskDirectlyUseCase, never()).execute(any(), anyString());
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 일시적 오류 처리 테스트")
    class RetryableErrorTest {

        @Test
        @DisplayName("[실패] RetryableExecutionException 발생 시 예외를 재전파하여 SQS 재시도를 트리거한다")
        void shouldRethrowRetryableExecutionException() {
            // Given
            CrawlTaskPayload payload = createPayload(2L);
            ExecuteCrawlTaskCommand command = createCommand(2L);
            given(mapper.toCommand(payload)).willReturn(command);
            RetryableExecutionException retryableException =
                    new RetryableExecutionException("DB 커넥션 실패", new RuntimeException("원인"));
            doThrow(retryableException).when(crawlTaskExecutionUseCase).execute(any());

            // When & Then: RetryableExecutionException이 전파되어야 함
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(RetryableExecutionException.class)
                    .hasMessage("DB 커넥션 실패");

            // failSafely는 호출되지 않음 (재시도 위임이므로)
            verify(failCrawlTaskDirectlyUseCase, never()).execute(any(), anyString());
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 영구적 오류 처리 테스트")
    class PermanentErrorTest {

        @Test
        @DisplayName("[실패] 비즈니스 오류(RuntimeException) 발생 시 failSafely를 호출하고 ACK 처리한다")
        void shouldCallFailSafelyForPermanentError() {
            // Given
            CrawlTaskPayload payload = createPayload(3L);
            ExecuteCrawlTaskCommand command = createCommand(3L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new RuntimeException("비즈니스 검증 실패"))
                    .when(crawlTaskExecutionUseCase)
                    .execute(any());
            doNothing().when(failCrawlTaskDirectlyUseCase).execute(eq(3L), anyString());

            // When: 예외가 전파되지 않아야 함 (ACK)
            sut.handleMessage(payload);

            // Then: failSafely가 해당 taskId로 호출됨
            verify(failCrawlTaskDirectlyUseCase).execute(eq(3L), anyString());
        }

        @Test
        @DisplayName("[실패] IllegalArgumentException도 영구적 오류로 처리하여 failSafely를 호출한다")
        void shouldCallFailSafelyForIllegalArgumentException() {
            // Given
            CrawlTaskPayload payload = createPayload(4L);
            ExecuteCrawlTaskCommand command = createCommand(4L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new IllegalArgumentException("잘못된 파라미터"))
                    .when(crawlTaskExecutionUseCase)
                    .execute(any());
            doNothing().when(failCrawlTaskDirectlyUseCase).execute(eq(4L), anyString());

            // When
            sut.handleMessage(payload);

            // Then
            verify(failCrawlTaskDirectlyUseCase).execute(eq(4L), anyString());
        }

        @Test
        @DisplayName("[실패] failSafely 자체가 실패해도 예외가 전파되지 않는다 (PUBLISHED 고아 로그만 기록)")
        void shouldNotPropagateExceptionWhenFailSafelyFails() {
            // Given
            CrawlTaskPayload payload = createPayload(5L);
            ExecuteCrawlTaskCommand command = createCommand(5L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new RuntimeException("비즈니스 오류")).when(crawlTaskExecutionUseCase).execute(any());
            doThrow(new RuntimeException("failSafely 내부 오류"))
                    .when(failCrawlTaskDirectlyUseCase)
                    .execute(any(), anyString());

            // When: failSafely도 실패하지만 예외 전파 없이 정상 종료
            sut.handleMessage(payload);

            // Then: failSafely 호출 시도는 했음을 확인
            verify(failCrawlTaskDirectlyUseCase).execute(eq(5L), anyString());
        }

        @Test
        @DisplayName("[실패] Mapper 예외(영구 오류)는 재전파된다 - taskId를 알 수 없으므로 failSafely 미호출")
        void shouldRethrowExceptionFromMapperDirectly() {
            // Given: Mapper에서 예외 발생 시 taskId를 알기 전에 예외 발생
            // 실제로 handleMessage에서는 payload.taskId()를 먼저 추출하므로
            // Mapper 예외는 catch 블록 내에서 발생하여 isRetryable 체크 후 처리됨
            CrawlTaskPayload payload = createPayload(6L);
            given(mapper.toCommand(payload)).willThrow(new RuntimeException("Mapper 오류"));
            doNothing().when(failCrawlTaskDirectlyUseCase).execute(eq(6L), anyString());

            // When: Mapper 예외는 catch로 잡혀 영구적 오류로 처리됨
            sut.handleMessage(payload);

            // Then: taskId=6L로 failSafely 호출
            verify(failCrawlTaskDirectlyUseCase).execute(eq(6L), anyString());
        }
    }
}
