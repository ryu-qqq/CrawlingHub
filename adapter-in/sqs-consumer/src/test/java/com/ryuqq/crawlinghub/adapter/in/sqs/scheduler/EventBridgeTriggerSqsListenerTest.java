package com.ryuqq.crawlinghub.adapter.in.sqs.scheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EventBridgeTriggerSqsListener 단위 테스트
 *
 * <p>EventBridge SQS 메시지 수신 후 처리 흐름을 검증합니다.
 *
 * <ul>
 *   <li>정상 메시지 처리: UseCase 호출 검증
 *   <li>영구적 오류(DomainException): 예외를 삼키고 ACK (재시도 무의미)
 *   <li>일시적 오류(그 외): 예외 재전파로 SQS 재시도 트리거
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("EventBridgeTriggerSqsListener 단위 테스트")
class EventBridgeTriggerSqsListenerTest {

    @Mock private EventBridgeTriggerListenerMapper mapper;
    @Mock private TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;

    private EventBridgeTriggerSqsListener sut;

    @BeforeEach
    void setUp() {
        sut = new EventBridgeTriggerSqsListener(mapper, triggerCrawlTaskUseCase);
    }

    private EventBridgeTriggerPayload createPayload(Long schedulerId) {
        return new EventBridgeTriggerPayload(
                schedulerId, 100L, "test-scheduler", "2026-02-23T10:00:00Z");
    }

    /**
     * 테스트용 DomainException 구현
     *
     * <p>DomainException은 protected 생성자를 사용하므로 테스트용 서브클래스를 정의합니다.
     */
    static class TestDomainException extends DomainException {

        TestDomainException() {
            super(
                    new ErrorCode() {
                        @Override
                        public String getCode() {
                            return "TEST-001";
                        }

                        @Override
                        public int getHttpStatus() {
                            return 400;
                        }

                        @Override
                        public String getMessage() {
                            return "테스트 도메인 예외";
                        }
                    });
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 정상 처리 테스트")
    class HandleMessageSuccessTest {

        @Test
        @DisplayName("[성공] 정상 페이로드 수신 시 Mapper와 UseCase가 호출된다")
        void shouldCallMapperAndUseCaseForValidPayload() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(1L);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(1L);
            given(mapper.toCommand(payload)).willReturn(command);
            doNothing().when(triggerCrawlTaskUseCase).execute(command);

            // When
            sut.handleMessage(payload);

            // Then
            verify(mapper).toCommand(payload);
            verify(triggerCrawlTaskUseCase).execute(command);
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 영구적 오류 처리 테스트")
    class PermanentErrorTest {

        @Test
        @DisplayName("[실패] DomainException 발생 시 예외를 삼키고 정상 반환한다 (ACK, 재시도 무의미)")
        void shouldSwallowDomainExceptionWithoutRethrow() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(2L);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(2L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new TestDomainException()).when(triggerCrawlTaskUseCase).execute(any());

            // When: 예외가 전파되지 않아야 함 (ACK 처리)
            sut.handleMessage(payload);

            // Then: UseCase까지 호출됨을 확인
            verify(triggerCrawlTaskUseCase).execute(any());
        }

        @Test
        @DisplayName("[실패] DomainException 서브클래스도 영구적 오류로 처리되어 예외를 삼킨다")
        void shouldSwallowDomainExceptionSubclass() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(3L);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(3L);
            given(mapper.toCommand(payload)).willReturn(command);

            // DomainException의 서브클래스 생성
            doThrow(new TestDomainException()).when(triggerCrawlTaskUseCase).execute(any());

            // When & Then: 예외 전파 없이 정상 반환
            sut.handleMessage(payload);
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 일시적 오류 처리 테스트")
    class TransientErrorTest {

        @Test
        @DisplayName("[실패] RuntimeException 발생 시 예외를 재전파하여 SQS 재시도를 트리거한다")
        void shouldRethrowRuntimeExceptionForSqsRetry() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(4L);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(4L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new RuntimeException("DB 커넥션 실패")).when(triggerCrawlTaskUseCase).execute(any());

            // When & Then: 예외가 재전파되어야 함 (SQS NACK → 재시도)
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB 커넥션 실패");
        }

        @Test
        @DisplayName("[실패] IllegalStateException도 일시적 오류로 재전파된다")
        void shouldRethrowIllegalStateException() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(5L);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(5L);
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new IllegalStateException("상태 오류"))
                    .when(triggerCrawlTaskUseCase)
                    .execute(any());

            // When & Then
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("상태 오류");
        }

        @Test
        @DisplayName("[실패] Mapper에서 예외 발생 시에도 재전파된다")
        void shouldRethrowExceptionFromMapper() {
            // Given
            EventBridgeTriggerPayload payload = createPayload(6L);
            given(mapper.toCommand(payload)).willThrow(new RuntimeException("Mapper 오류"));

            // When & Then
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Mapper 오류");

            // UseCase는 호출되지 않음
            verify(triggerCrawlTaskUseCase, never()).execute(any());
        }
    }
}
