package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ProductCountHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ScheduleInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SellerApiMapper 단위 테스트
 *
 * <p>REST API DTO ↔ Application DTO 변환 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("SellerApiMapper 단위 테스트")
class SellerApiMapperTest {

    private SellerApiMapper sut;

    @BeforeEach
    void setUp() {
        sut = new SellerApiMapper();
    }

    @Nested
    @DisplayName("toCommand 메서드는")
    class Describe_toCommand {

        @Nested
        @DisplayName("유효한 RegisterSellerApiRequest가 주어지면")
        class Context_with_valid_request {

            private RegisterSellerApiRequest request;

            @BeforeEach
            void setUp() {
                request = new RegisterSellerApiRequest(
                    "SELLER001",
                    "Test Seller",
                    "DAILY",
                    1
                );
            }

            @Test
            @DisplayName("RegisterSellerCommand로 변환한다")
            void it_converts_to_register_seller_command() {
                // When
                RegisterSellerCommand command = sut.toCommand(request);

                // Then
                assertThat(command).isNotNull();
                assertThat(command.sellerCode()).isEqualTo("SELLER001");
                assertThat(command.sellerName()).isEqualTo("Test Seller");
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                RegisterSellerCommand command = sut.toCommand(null);

                // Then
                assertThat(command).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toResponse 메서드는")
    class Describe_toResponse {

        @Nested
        @DisplayName("유효한 SellerResponse가 주어지면")
        class Context_with_valid_seller_response {

            private SellerResponse sellerResponse;

            @BeforeEach
            void setUp() {
                sellerResponse = new SellerResponse(
                    1L,
                    "SELLER001",
                    "Test Seller",
                    SellerStatus.ACTIVE,
                    0,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                );
            }

            @Test
            @DisplayName("RegisterSellerApiResponse로 변환한다")
            void it_converts_to_register_seller_api_response() {
                // When
                RegisterSellerApiResponse response = sut.toResponse(sellerResponse);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerId()).isEqualTo("1");
                assertThat(response.name()).isEqualTo("Test Seller");
                assertThat(response.isActive()).isTrue();
                assertThat(response.intervalType()).isEqualTo("DAILY");
                assertThat(response.intervalValue()).isEqualTo(1);
                assertThat(response.createdAt()).isNotNull();
            }

            @Test
            @DisplayName("ACTIVE 상태는 isActive=true로 변환된다")
            void active_status_is_converted_to_true() {
                // When
                RegisterSellerApiResponse response = sut.toResponse(sellerResponse);

                // Then
                assertThat(response.isActive()).isTrue();
            }
        }

        @Nested
        @DisplayName("PAUSED 상태의 SellerResponse가 주어지면")
        class Context_with_paused_seller_response {

            private SellerResponse sellerResponse;

            @BeforeEach
            void setUp() {
                sellerResponse = new SellerResponse(
                    1L,
                    "SELLER001",
                    "Test Seller",
                    SellerStatus.PAUSED,
                    0,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                );
            }

            @Test
            @DisplayName("isActive=false로 변환한다")
            void paused_status_is_converted_to_false() {
                // When
                RegisterSellerApiResponse response = sut.toResponse(sellerResponse);

                // Then
                assertThat(response.isActive()).isFalse();
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                RegisterSellerApiResponse response = sut.toResponse(null);

                // Then
                assertThat(response).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toUpdateCommand 메서드는")
    class Describe_toUpdateCommand {

        @Nested
        @DisplayName("isActive=true인 UpdateSellerApiRequest가 주어지면")
        class Context_with_active_true {

            private UpdateSellerApiRequest request;
            private Long sellerId = 1L;

            @BeforeEach
            void setUp() {
                request = new UpdateSellerApiRequest(true, "DAILY", 1);
            }

            @Test
            @DisplayName("SellerStatus.ACTIVE로 변환한다")
            void it_converts_to_active_status() {
                // When
                UpdateSellerStatusCommand command = sut.toUpdateCommand(sellerId, request);

                // Then
                assertThat(command).isNotNull();
                assertThat(command.sellerId()).isEqualTo(1L);
                assertThat(command.status()).isEqualTo(SellerStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("isActive=false인 UpdateSellerApiRequest가 주어지면")
        class Context_with_active_false {

            private UpdateSellerApiRequest request;
            private Long sellerId = 1L;

            @BeforeEach
            void setUp() {
                request = new UpdateSellerApiRequest(false, "DAILY", 1);
            }

            @Test
            @DisplayName("SellerStatus.PAUSED로 변환한다")
            void it_converts_to_paused_status() {
                // When
                UpdateSellerStatusCommand command = sut.toUpdateCommand(sellerId, request);

                // Then
                assertThat(command).isNotNull();
                assertThat(command.sellerId()).isEqualTo(1L);
                assertThat(command.status()).isEqualTo(SellerStatus.PAUSED);
            }
        }

        @Nested
        @DisplayName("isActive가 null인 UpdateSellerApiRequest가 주어지면")
        class Context_with_null_is_active {

            private UpdateSellerApiRequest request;
            private Long sellerId = 1L;

            @BeforeEach
            void setUp() {
                request = new UpdateSellerApiRequest(null, "DAILY", 1);
            }

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // When & Then
                assertThatThrownBy(() -> sut.toUpdateCommand(sellerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("isActive 필드는 필수입니다");
            }
        }

        @Nested
        @DisplayName("null 요청이 주어지면")
        class Context_with_null_request {

            private Long sellerId = 1L;

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                UpdateSellerStatusCommand command = sut.toUpdateCommand(sellerId, null);

                // Then
                assertThat(command).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toUpdateResponse 메서드는")
    class Describe_toUpdateResponse {

        @Nested
        @DisplayName("유효한 SellerResponse가 주어지면")
        class Context_with_valid_seller_response {

            private SellerResponse sellerResponse;

            @BeforeEach
            void setUp() {
                sellerResponse = new SellerResponse(
                    1L,
                    "SELLER001",
                    "Test Seller",
                    SellerStatus.ACTIVE,
                    0,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                );
            }

            @Test
            @DisplayName("UpdateSellerApiResponse로 변환한다")
            void it_converts_to_update_seller_api_response() {
                // When
                UpdateSellerApiResponse response = sut.toUpdateResponse(sellerResponse);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerId()).isEqualTo("1");
                assertThat(response.name()).isEqualTo("Test Seller");
                assertThat(response.isActive()).isTrue();
                assertThat(response.updatedAt()).isNotNull();
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                UpdateSellerApiResponse response = sut.toUpdateResponse(null);

                // Then
                assertThat(response).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toProductCountHistoryApiResponse 메서드는")
    class Describe_toProductCountHistoryApiResponse {

        @Nested
        @DisplayName("유효한 ProductCountHistoryResponse가 주어지면")
        class Context_with_valid_response {

            private ProductCountHistoryResponse response;

            @BeforeEach
            void setUp() {
                response = new ProductCountHistoryResponse(
                    1L,
                    LocalDateTime.now(),
                    100
                );
            }

            @Test
            @DisplayName("ProductCountHistoryApiResponse로 변환한다")
            void it_converts_to_api_response() {
                // When
                ProductCountHistoryApiResponse apiResponse = sut.toProductCountHistoryApiResponse(response);

                // Then
                assertThat(apiResponse).isNotNull();
                assertThat(apiResponse.historyId()).isEqualTo(1L);
                assertThat(apiResponse.executedDate()).isNotNull();
                assertThat(apiResponse.productCount()).isEqualTo(100);
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                ProductCountHistoryApiResponse response = sut.toProductCountHistoryApiResponse(null);

                // Then
                assertThat(response).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toProductCountHistoryPageApiResponse 메서드는")
    class Describe_toProductCountHistoryPageApiResponse {

        @Nested
        @DisplayName("유효한 PageResponse가 주어지면")
        class Context_with_valid_page_response {

            private PageResponse<ProductCountHistoryResponse> pageResponse;

            @BeforeEach
            void setUp() {
                List<ProductCountHistoryResponse> content = List.of(
                    new ProductCountHistoryResponse(1L, LocalDateTime.now(), 100),
                    new ProductCountHistoryResponse(2L, LocalDateTime.now().minusDays(1), 90)
                );

                pageResponse = PageResponse.of(
                    content,
                    0,
                    10,
                    2L,
                    1,
                    true,
                    true
                );
            }

            @Test
            @DisplayName("PageApiResponse로 변환한다")
            void it_converts_to_page_api_response() {
                // When
                PageApiResponse<ProductCountHistoryApiResponse> apiResponse =
                    sut.toProductCountHistoryPageApiResponse(pageResponse);

                // Then
                assertThat(apiResponse).isNotNull();
                assertThat(apiResponse.content()).hasSize(2);
                assertThat(apiResponse.page()).isEqualTo(0);
                assertThat(apiResponse.size()).isEqualTo(10);
                assertThat(apiResponse.totalElements()).isEqualTo(2L);
                assertThat(apiResponse.totalPages()).isEqualTo(1);
                assertThat(apiResponse.first()).isTrue();
                assertThat(apiResponse.last()).isTrue();
            }

            @Test
            @DisplayName("모든 content가 API Response로 변환된다")
            void all_content_is_converted() {
                // When
                PageApiResponse<ProductCountHistoryApiResponse> apiResponse =
                    sut.toProductCountHistoryPageApiResponse(pageResponse);

                // Then
                assertThat(apiResponse.content().get(0).historyId()).isEqualTo(1L);
                assertThat(apiResponse.content().get(1).historyId()).isEqualTo(2L);
            }
        }

        @Nested
        @DisplayName("빈 PageResponse가 주어지면")
        class Context_with_empty_page_response {

            private PageResponse<ProductCountHistoryResponse> pageResponse;

            @BeforeEach
            void setUp() {
                pageResponse = PageResponse.of(
                    Collections.emptyList(),
                    0,
                    10,
                    0L,
                    0,
                    true,
                    true
                );
            }

            @Test
            @DisplayName("빈 PageApiResponse로 변환한다")
            void it_converts_to_empty_page_api_response() {
                // When
                PageApiResponse<ProductCountHistoryApiResponse> apiResponse =
                    sut.toProductCountHistoryPageApiResponse(pageResponse);

                // Then
                assertThat(apiResponse).isNotNull();
                assertThat(apiResponse.content()).isEmpty();
                assertThat(apiResponse.totalElements()).isZero();
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                PageApiResponse<ProductCountHistoryApiResponse> response =
                    sut.toProductCountHistoryPageApiResponse(null);

                // Then
                assertThat(response).isNull();
            }
        }
    }

    @Nested
    @DisplayName("toScheduleInfoApiResponse 메서드는")
    class Describe_toScheduleInfoApiResponse {

        @Nested
        @DisplayName("유효한 ScheduleInfoResponse가 주어지면")
        class Context_with_valid_response {

            private ScheduleInfoResponse response;

            @BeforeEach
            void setUp() {
                response = new ScheduleInfoResponse(
                    1L,
                    "0 0 * * *",
                    "ACTIVE",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now()
                );
            }

            @Test
            @DisplayName("ScheduleInfoApiResponse로 변환한다")
            void it_converts_to_api_response() {
                // When
                ScheduleInfoApiResponse apiResponse = sut.toScheduleInfoApiResponse(response);

                // Then
                assertThat(apiResponse).isNotNull();
                assertThat(apiResponse.scheduleId()).isEqualTo(1L);
                assertThat(apiResponse.cronExpression()).isEqualTo("0 0 * * *");
                assertThat(apiResponse.status()).isEqualTo("ACTIVE");
                assertThat(apiResponse.nextExecutionTime()).isNotNull();
                assertThat(apiResponse.createdAt()).isNotNull();
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                ScheduleInfoApiResponse response = sut.toScheduleInfoApiResponse(null);

                // Then
                assertThat(response).isNull();
            }
        }
    }
}
