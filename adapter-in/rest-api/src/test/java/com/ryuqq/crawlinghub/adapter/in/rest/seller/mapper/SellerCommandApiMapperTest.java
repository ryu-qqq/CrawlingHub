package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
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
 *   <li>필드 매핑 정확성
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
        @DisplayName("성공: active=true로 업데이트")
        void toCommand_UpdateSeller_ActiveTrue() {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest("머스트잇 셀러명", "셀러명", true);

            // When
            UpdateSellerCommand command = mapper.toCommand(sellerId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.sellerId()).isEqualTo(1L);
            assertThat(command.active()).isTrue();
        }
    }
}
