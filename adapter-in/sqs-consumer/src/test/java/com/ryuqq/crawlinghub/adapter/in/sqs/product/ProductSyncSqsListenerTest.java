package com.ryuqq.crawlinghub.adapter.in.sqs.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessProductSyncFromSqsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductSyncSqsListener 단위 테스트
 *
 * <p>SQS 메시지 수신 후 처리 흐름을 검증합니다.
 *
 * <ul>
 *   <li>정상 메시지 처리 시 UseCase가 호출되는지 확인
 *   <li>예외 발생 시 SQS 재시도를 위해 예외가 재전파되는지 확인
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSyncSqsListener 단위 테스트")
class ProductSyncSqsListenerTest {

    @Mock private ProductSyncListenerMapper mapper;
    @Mock private ProcessProductSyncFromSqsUseCase processProductSyncFromSqsUseCase;

    private ProductSyncSqsListener sut;

    @BeforeEach
    void setUp() {
        sut = new ProductSyncSqsListener(mapper, processProductSyncFromSqsUseCase);
    }

    /** 테스트용 ProductSyncPayload 생성 */
    private ProductSyncPayload createPayload(
            Long outboxId,
            Long crawledProductId,
            Long sellerId,
            Long itemNo,
            String syncType,
            Long externalProductId,
            String idempotencyKey) {
        return new ProductSyncPayload(
                outboxId,
                crawledProductId,
                sellerId,
                itemNo,
                syncType,
                externalProductId,
                idempotencyKey);
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 정상 처리 테스트")
    class HandleMessageSuccessTest {

        @Test
        @DisplayName("[성공] 정상 메시지 수신 시 Mapper와 UseCase가 순서대로 호출된다")
        void shouldCallMapperAndUseCaseForValidPayload() {
            // Given
            ProductSyncPayload payload =
                    createPayload(1L, 10L, 100L, 1000L, "CREATE", null, "idem-001");
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(1L, 10L, 100L, 1000L, "CREATE", null, "idem-001");
            given(mapper.toCommand(payload)).willReturn(command);

            // When
            sut.handleMessage(payload);

            // Then
            verify(mapper).toCommand(payload);
            verify(processProductSyncFromSqsUseCase).execute(command);
        }

        @Test
        @DisplayName("[성공] UPDATE 타입 메시지도 올바르게 처리된다")
        void shouldHandleUpdateTypeMessage() {
            // Given
            ProductSyncPayload payload =
                    createPayload(2L, 20L, 200L, 2000L, "UPDATE", 999L, "idem-002");
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(2L, 20L, 200L, 2000L, "UPDATE", 999L, "idem-002");
            given(mapper.toCommand(payload)).willReturn(command);

            // When
            sut.handleMessage(payload);

            // Then
            ArgumentCaptor<ProcessProductSyncCommand> captor =
                    forClass(ProcessProductSyncCommand.class);
            verify(processProductSyncFromSqsUseCase).execute(captor.capture());
            assertThat(captor.getValue().syncType()).isEqualTo("UPDATE");
            assertThat(captor.getValue().externalProductId()).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("handleMessage() 메서드 - 예외 처리 테스트")
    class HandleMessageExceptionTest {

        @Test
        @DisplayName("[실패] UseCase 예외 발생 시 예외가 재전파되어 SQS 재시도를 트리거한다")
        void shouldRethrowExceptionForSqsRetry() {
            // Given
            ProductSyncPayload payload =
                    createPayload(3L, 30L, 300L, 3000L, "CREATE", null, "idem-003");
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(3L, 30L, 300L, 3000L, "CREATE", null, "idem-003");
            given(mapper.toCommand(payload)).willReturn(command);
            doThrow(new RuntimeException("인프라 오류"))
                    .when(processProductSyncFromSqsUseCase)
                    .execute(any());

            // When & Then: 예외가 전파되어야 함 (SQS NACK → 재시도)
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("인프라 오류");
        }

        @Test
        @DisplayName("[실패] Mapper 예외 발생 시에도 예외가 재전파된다")
        void shouldRethrowExceptionWhenMapperFails() {
            // Given
            ProductSyncPayload payload =
                    createPayload(4L, 40L, 400L, 4000L, "CREATE", null, "idem-004");
            given(mapper.toCommand(payload)).willThrow(new IllegalArgumentException("페이로드 변환 실패"));

            // When & Then
            assertThatThrownBy(() -> sut.handleMessage(payload))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("페이로드 변환 실패");
        }
    }
}
