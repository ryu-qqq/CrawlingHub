package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.RateLimiterPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * InitializePoolUseCase 단위 테스트
 *
 * 검증 사항:
 * - Redis 작업(clearPool, addToPool, initialize)이 트랜잭션 밖에서 실행됨
 * - DB 조회(findAllActiveUserAgents)만 내부 트랜잭션으로 처리됨
 * - 실행 순서: clearPool → DB 조회 → Pool 등록 → Rate Limiter 초기화
 *
 * @author crawlinghub
 */
@ExtendWith(MockitoExtension.class)
class InitializePoolUseCaseTest {

    @Mock
    private UserAgentPoolPort poolPort;

    @Mock
    private UserAgentTokenPort tokenPort;

    @Mock
    private RateLimiterPort rateLimiterPort;

    @InjectMocks
    private InitializePoolUseCase useCase;

    @Test
    @DisplayName("Pool 초기화 성공 - 활성 User-Agent 등록")
    void initializePoolSuccess() {
        // given
        List<Long> activeUserAgents = List.of(1L, 2L, 3L, 4L, 5L);
        when(tokenPort.findAllActiveUserAgents()).thenReturn(activeUserAgents);

        // when
        int result = useCase.execute();

        // then
        assertThat(result).isEqualTo(5);

        // Pool 초기화 확인
        verify(poolPort).clearPool();

        // 각 User-Agent가 Pool에 등록되었는지 확인
        for (Long userAgentId : activeUserAgents) {
            verify(poolPort).addToPool(userAgentId);
            verify(rateLimiterPort).initialize(userAgentId);
        }
    }

    @Test
    @DisplayName("Pool 초기화 - 활성 User-Agent 없음")
    void initializePoolWithEmptyUserAgents() {
        // given
        when(tokenPort.findAllActiveUserAgents()).thenReturn(List.of());

        // when
        int result = useCase.execute();

        // then
        assertThat(result).isEqualTo(0);
        verify(poolPort).clearPool();
        verify(poolPort, never()).addToPool(anyLong());
        verify(rateLimiterPort, never()).initialize(anyLong());
    }

    @Test
    @DisplayName("Pool 초기화 - 순서 검증")
    void initializePoolVerifyOrder() {
        // given
        List<Long> activeUserAgents = List.of(1L, 2L);
        when(tokenPort.findAllActiveUserAgents()).thenReturn(activeUserAgents);

        // when
        useCase.execute();

        // then
        InOrder inOrder = inOrder(poolPort, tokenPort, rateLimiterPort);

        // 1. Pool 초기화가 먼저 실행
        inOrder.verify(poolPort).clearPool();

        // 2. DB에서 활성 User-Agent 조회
        inOrder.verify(tokenPort).findAllActiveUserAgents();

        // 3. 각 User-Agent 등록 (Pool → Rate Limiter 순서)
        inOrder.verify(poolPort).addToPool(1L);
        inOrder.verify(rateLimiterPort).initialize(1L);
        inOrder.verify(poolPort).addToPool(2L);
        inOrder.verify(rateLimiterPort).initialize(2L);
    }

    @Test
    @DisplayName("Pool 초기화 - 대량 User-Agent 처리")
    void initializePoolWithLargeUserAgents() {
        // given
        List<Long> activeUserAgents = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L);
        when(tokenPort.findAllActiveUserAgents()).thenReturn(activeUserAgents);

        // when
        int result = useCase.execute();

        // then
        assertThat(result).isEqualTo(20);
        verify(poolPort).clearPool();
        verify(poolPort, times(20)).addToPool(anyLong());
        verify(rateLimiterPort, times(20)).initialize(anyLong());
    }
}
