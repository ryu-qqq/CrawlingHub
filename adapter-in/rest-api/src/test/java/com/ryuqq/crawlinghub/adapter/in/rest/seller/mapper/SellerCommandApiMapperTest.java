package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SellerCommandApiMapper 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>API Request → Application Command 변환
 *   <li>Application Response → API Response 변환
 *   <li>필드 매핑 정확성
 *   <li>null 값 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerCommandApiMapper 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class SellerCommandApiMapperTest {

    private SellerCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SellerCommandApiMapper();
    }

    @Nested
    @DisplayName("toCommand(RegisterSellerApiRequest) - 셀러 등록 요청 변환")
    class ToRegisterCommandTests {

        @Test
        @DisplayName("성공: API Request → RegisterSellerCommand 변환")
        void toCommand_RegisterSeller_Success() {
            // Given
            RegisterSellerApiRequest request =
                    new RegisterSellerApiRequest("머스트잇 테스트 셀러", "테스트 셀러");

            // When
            RegisterSellerCommand command = mapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.mustItSellerName()).isEqualTo("머스트잇 테스트 셀러");
            assertThat(command.sellerName()).isEqualTo("테스트 셀러");
        }

        @Test
        @DisplayName("성공: 특수문자 포함된 이름 변환")
        void toCommand_RegisterSeller_WithSpecialCharacters() {
            // Given
            RegisterSellerApiRequest request =
                    new RegisterSellerApiRequest("머스트잇-테스트_셀러 (주)", "테스트@셀러#123");

            // When
            RegisterSellerCommand command = mapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.mustItSellerName()).isEqualTo("머스트잇-테스트_셀러 (주)");
            assertThat(command.sellerName()).isEqualTo("테스트@셀러#123");
        }

        @Test
        @DisplayName("성공: 최대 길이 이름 변환")
        void toCommand_RegisterSeller_MaxLength() {
            // Given
            String maxLengthName = "a".repeat(100);
            RegisterSellerApiRequest request =
                    new RegisterSellerApiRequest(maxLengthName, maxLengthName);

            // When
            RegisterSellerCommand command = mapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.mustItSellerName()).hasSize(100);
            assertThat(command.sellerName()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("toCommand(Long, UpdateSellerApiRequest) - 셀러 수정 요청 변환")
    class ToUpdateCommandTests {

        @Test
        @DisplayName("성공: 모든 필드 업데이트")
        void toCommand_UpdateSeller_AllFields() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request =
                    new UpdateSellerApiRequest("수정된 머스트잇 셀러명", "수정된 셀러명", false);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.mustItSellerName()).isEqualTo("수정된 머스트잇 셀러명");
            assertThat(command.sellerName()).isEqualTo("수정된 셀러명");
            assertThat(command.active()).isFalse();
        }

        @Test
        @DisplayName("성공: mustItSellerName만 업데이트 (부분 업데이트)")
        void toCommand_UpdateSeller_MustItSellerNameOnly() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest("새로운 머스트잇 셀러명", null, null);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.mustItSellerName()).isEqualTo("새로운 머스트잇 셀러명");
            assertThat(command.sellerName()).isNull();
            assertThat(command.active()).isNull();
        }

        @Test
        @DisplayName("성공: sellerName만 업데이트")
        void toCommand_UpdateSeller_SellerNameOnly() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, "새로운 셀러명", null);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.mustItSellerName()).isNull();
            assertThat(command.sellerName()).isEqualTo("새로운 셀러명");
            assertThat(command.active()).isNull();
        }

        @Test
        @DisplayName("성공: active만 업데이트 (활성화)")
        void toCommand_UpdateSeller_ActiveOnly_True() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, null, true);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.mustItSellerName()).isNull();
            assertThat(command.sellerName()).isNull();
            assertThat(command.active()).isTrue();
        }

        @Test
        @DisplayName("성공: active만 업데이트 (비활성화)")
        void toCommand_UpdateSeller_ActiveOnly_False() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, null, false);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.active()).isFalse();
        }

        @Test
        @DisplayName("성공: 모든 필드가 null인 경우")
        void toCommand_UpdateSeller_AllFieldsNull() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, null, null);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.mustItSellerName()).isNull();
            assertThat(command.sellerName()).isNull();
            assertThat(command.active()).isNull();
        }
    }

    @Nested
    @DisplayName("toApiResponse(SellerResponse) - Application Response → API Response 변환")
    class ToApiResponseTests {

        @Test
        @DisplayName("성공: active=true → status=ACTIVE 변환")
        void toApiResponse_ActiveTrue() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            1L,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            true,
                            Instant.parse("2024-11-27T10:00:00Z"),
                            null);

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(1L);
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇 테스트 셀러");
            assertThat(apiResponse.sellerName()).isEqualTo("테스트 셀러");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
            assertThat(apiResponse.createdAt()).isEqualTo("2024-11-27T10:00:00Z");
            assertThat(apiResponse.updatedAt()).isNull();
        }

        @Test
        @DisplayName("성공: active=false → status=INACTIVE 변환")
        void toApiResponse_ActiveFalse() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            2L,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            false,
                            Instant.parse("2024-11-27T10:00:00Z"),
                            Instant.parse("2024-11-27T11:00:00Z"));

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(2L);
            assertThat(apiResponse.status()).isEqualTo("INACTIVE");
            assertThat(apiResponse.createdAt()).isEqualTo("2024-11-27T10:00:00Z");
            assertThat(apiResponse.updatedAt()).isEqualTo("2024-11-27T11:00:00Z");
        }

        @Test
        @DisplayName("성공: updatedAt이 null인 경우 (신규 생성)")
        void toApiResponse_UpdatedAtNull() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            1L,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            true,
                            Instant.parse("2024-11-27T10:00:00Z"),
                            null);

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.updatedAt()).isNull();
        }

        @Test
        @DisplayName("성공: updatedAt이 있는 경우 (수정됨)")
        void toApiResponse_UpdatedAtPresent() {
            // Given
            Instant createdAt = Instant.parse("2024-11-27T10:00:00Z");
            Instant updatedAt = Instant.parse("2024-11-27T15:30:00Z");

            SellerResponse appResponse =
                    new SellerResponse(1L, "머스트잇 테스트 셀러", "테스트 셀러", true, createdAt, updatedAt);

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.createdAt()).isEqualTo("2024-11-27T10:00:00Z");
            assertThat(apiResponse.updatedAt()).isEqualTo("2024-11-27T15:30:00Z");
            // API Response는 String이므로 Instant로 파싱하여 시간 순서 검증
            assertThat(Instant.parse(apiResponse.updatedAt()))
                    .isAfter(Instant.parse(apiResponse.createdAt()));
        }

        @Test
        @DisplayName("성공: 특수문자 포함된 이름 변환")
        void toApiResponse_WithSpecialCharacters() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            1L, "머스트잇-테스트_셀러 (주)", "테스트@셀러#123", true, Instant.now(), null);

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇-테스트_셀러 (주)");
            assertThat(apiResponse.sellerName()).isEqualTo("테스트@셀러#123");
        }
    }
}
