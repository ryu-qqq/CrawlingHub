package com.ryuqq.crawlinghub.application.useragent.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetUserAgentsService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserAgentsService 테스트")
class GetUserAgentsServiceTest {

    @Mock private UserAgentQueryPort userAgentQueryPort;

    @InjectMocks private GetUserAgentsService service;

    @Nested
    @DisplayName("execute() UserAgent 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 전체 UserAgent 목록 조회")
        void shouldReturnAllUserAgents() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);
            UserAgentSearchCriteria criteria = UserAgentSearchCriteria.all(pageRequest);
            Instant now = Instant.now();

            List<UserAgentSummaryResponse> content =
                    List.of(
                            UserAgentSummaryResponse.of(
                                    1L,
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                                    DeviceType.of("DESKTOP"),
                                    UserAgentStatus.AVAILABLE,
                                    95,
                                    150,
                                    now,
                                    now,
                                    now),
                            UserAgentSummaryResponse.of(
                                    2L,
                                    "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)",
                                    DeviceType.of("MOBILE"),
                                    UserAgentStatus.SUSPENDED,
                                    60,
                                    80,
                                    now,
                                    now,
                                    now));

            PageResponse<UserAgentSummaryResponse> expectedResponse =
                    PageResponse.of(content, 0, 20, 2, 1, true, true);

            given(userAgentQueryPort.findByCriteria(criteria)).willReturn(expectedResponse);

            // When
            PageResponse<UserAgentSummaryResponse> result = service.execute(criteria);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            then(userAgentQueryPort).should().findByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 상태별 UserAgent 목록 조회 (AVAILABLE)")
        void shouldReturnUserAgentsByStatus() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            UserAgentSearchCriteria criteria =
                    UserAgentSearchCriteria.byStatus(UserAgentStatus.AVAILABLE, pageRequest);
            Instant now = Instant.now();

            List<UserAgentSummaryResponse> content =
                    List.of(
                            UserAgentSummaryResponse.of(
                                    1L,
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                                    DeviceType.of("DESKTOP"),
                                    UserAgentStatus.AVAILABLE,
                                    95,
                                    150,
                                    now,
                                    now,
                                    now));

            PageResponse<UserAgentSummaryResponse> expectedResponse =
                    PageResponse.of(content, 0, 10, 1, 1, true, true);

            given(userAgentQueryPort.findByCriteria(criteria)).willReturn(expectedResponse);

            // When
            PageResponse<UserAgentSummaryResponse> result = service.execute(criteria);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(UserAgentStatus.AVAILABLE);
            then(userAgentQueryPort).should().findByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 빈 결과 반환")
        void shouldReturnEmptyResult() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);
            UserAgentSearchCriteria criteria =
                    UserAgentSearchCriteria.byStatus(UserAgentStatus.BLOCKED, pageRequest);

            PageResponse<UserAgentSummaryResponse> expectedResponse =
                    PageResponse.of(List.of(), 0, 20, 0, 0, true, true);

            given(userAgentQueryPort.findByCriteria(criteria)).willReturn(expectedResponse);

            // When
            PageResponse<UserAgentSummaryResponse> result = service.execute(criteria);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("[성공] 페이징 정보 확인 (2페이지)")
        void shouldReturnCorrectPagingInfo() {
            // Given
            PageRequest pageRequest = PageRequest.of(1, 10);
            UserAgentSearchCriteria criteria = UserAgentSearchCriteria.all(pageRequest);
            Instant now = Instant.now();

            List<UserAgentSummaryResponse> content =
                    List.of(
                            UserAgentSummaryResponse.of(
                                    11L,
                                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
                                    DeviceType.of("DESKTOP"),
                                    UserAgentStatus.AVAILABLE,
                                    88,
                                    120,
                                    now,
                                    now,
                                    now));

            PageResponse<UserAgentSummaryResponse> expectedResponse =
                    PageResponse.of(content, 1, 10, 25, 3, false, false);

            given(userAgentQueryPort.findByCriteria(criteria)).willReturn(expectedResponse);

            // When
            PageResponse<UserAgentSummaryResponse> result = service.execute(criteria);

            // Then
            assertThat(result.page()).isEqualTo(1);
            assertThat(result.totalElements()).isEqualTo(25);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] SUSPENDED 상태 필터링")
        void shouldFilterBySuspendedStatus() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);
            UserAgentSearchCriteria criteria =
                    UserAgentSearchCriteria.byStatus(UserAgentStatus.SUSPENDED, pageRequest);
            Instant now = Instant.now();

            List<UserAgentSummaryResponse> content =
                    List.of(
                            UserAgentSummaryResponse.of(
                                    5L,
                                    "Mozilla/5.0 (Linux; Android 11)",
                                    DeviceType.of("MOBILE"),
                                    UserAgentStatus.SUSPENDED,
                                    45,
                                    200,
                                    now,
                                    now,
                                    now));

            PageResponse<UserAgentSummaryResponse> expectedResponse =
                    PageResponse.of(content, 0, 20, 1, 1, true, true);

            given(userAgentQueryPort.findByCriteria(criteria)).willReturn(expectedResponse);

            // When
            PageResponse<UserAgentSummaryResponse> result = service.execute(criteria);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(UserAgentStatus.SUSPENDED);
            assertThat(result.content().get(0).healthScore()).isEqualTo(45);
        }
    }
}
