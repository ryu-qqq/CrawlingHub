package com.ryuqq.crawlinghub.adapter.in.scheduler.useragent;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentPoolUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolWarmUpInitializer 단위 테스트
 *
 * <p>ApplicationReadyEvent 발생 시 UserAgent Pool WarmUp 로직을 검증합니다.
 *
 * <ul>
 *   <li>정상 WarmUp 완료 시 로그 출력 검증
 *   <li>WarmUp 0건 반환 시 정상 처리
 *   <li>WarmUp 실패(예외) 시 예외가 전파되지 않고 로그만 기록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolWarmUpInitializer 단위 테스트")
class UserAgentPoolWarmUpInitializerTest {

    @Mock private WarmUpUserAgentPoolUseCase warmUpUseCase;

    private UserAgentPoolWarmUpInitializer sut;

    @BeforeEach
    void setUp() {
        sut = new UserAgentPoolWarmUpInitializer(warmUpUseCase);
    }

    @Nested
    @DisplayName("onApplicationReady() 메서드 테스트")
    class OnApplicationReadyTest {

        @Test
        @DisplayName("[성공] WarmUp이 완료되면 UseCase를 호출한다")
        void shouldCallWarmUpUseCaseOnApplicationReady() {
            // Given
            given(warmUpUseCase.execute()).willReturn(10);

            // When
            sut.onApplicationReady();

            // Then
            verify(warmUpUseCase).execute();
        }

        @Test
        @DisplayName("[성공] WarmUp 대상이 없어도(0건) 예외 없이 처리된다")
        void shouldHandleZeroWarmUpCount() {
            // Given
            given(warmUpUseCase.execute()).willReturn(0);

            // When: 예외 발생 없이 완료되어야 함
            sut.onApplicationReady();

            // Then
            verify(warmUpUseCase).execute();
        }

        @Test
        @DisplayName("[실패] WarmUp UseCase 예외 발생 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenWarmUpFails() {
            // Given: WarmUp 실패 시나리오
            doThrow(new RuntimeException("Redis 연결 실패")).when(warmUpUseCase).execute();

            // When: 예외가 전파되지 않아야 함 (Housekeeper가 보완 처리)
            sut.onApplicationReady();

            // Then: UseCase가 호출되었음을 확인
            verify(warmUpUseCase).execute();
        }
    }
}
