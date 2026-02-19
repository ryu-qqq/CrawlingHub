package com.ryuqq.crawlinghub.adapter.in.rest.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.ManualSyncTriggerApiResponse;
import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductCommandApiMapper 단위 테스트
 *
 * <p>CrawledProduct Command REST API ↔ Application Layer DTO 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>crawledProductId → TriggerManualSyncCommand 변환
 *   <li>ManualSyncTriggerResponse → ManualSyncTriggerApiResponse 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawledProductCommandApiMapper 단위 테스트")
class CrawledProductCommandApiMapperTest {

    private CrawledProductCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawledProductCommandApiMapper();
    }

    @Nested
    @DisplayName("toTriggerManualSyncCommand()는")
    class ToTriggerManualSyncCommand {

        @Test
        @DisplayName("crawledProductId를 TriggerManualSyncCommand로 변환한다")
        void shouldConvertCrawledProductIdToCommand() {
            // Given
            Long crawledProductId = 42L;

            // When
            TriggerManualSyncCommand result = mapper.toTriggerManualSyncCommand(crawledProductId);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("다른 crawledProductId 값도 정상 변환한다")
        void shouldConvertDifferentCrawledProductId() {
            // Given
            Long crawledProductId = 999L;

            // When
            TriggerManualSyncCommand result = mapper.toTriggerManualSyncCommand(crawledProductId);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("toApiResponse()는")
    class ToApiResponse {

        @Test
        @DisplayName("ManualSyncTriggerResponse를 ManualSyncTriggerApiResponse로 변환한다")
        void shouldConvertManualSyncTriggerResponseToApiResponse() {
            // Given
            ManualSyncTriggerResponse appResponse =
                    new ManualSyncTriggerResponse(1L, 100L, "CREATE", "동기화 요청이 등록되었습니다.");

            // When
            ManualSyncTriggerApiResponse result = mapper.toApiResponse(appResponse);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(1L);
            assertThat(result.syncOutboxId()).isEqualTo(100L);
            assertThat(result.syncType()).isEqualTo("CREATE");
            assertThat(result.message()).isEqualTo("동기화 요청이 등록되었습니다.");
        }

        @Test
        @DisplayName("UPDATE 타입의 동기화 응답을 정상 변환한다")
        void shouldConvertUpdateTypeSyncResponse() {
            // Given
            ManualSyncTriggerResponse appResponse =
                    new ManualSyncTriggerResponse(55L, 200L, "UPDATE", "동기화 요청이 등록되었습니다.");

            // When
            ManualSyncTriggerApiResponse result = mapper.toApiResponse(appResponse);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(55L);
            assertThat(result.syncOutboxId()).isEqualTo(200L);
            assertThat(result.syncType()).isEqualTo("UPDATE");
            assertThat(result.message()).isEqualTo("동기화 요청이 등록되었습니다.");
        }

        @Test
        @DisplayName("스킵된 동기화 응답(null syncOutboxId, null syncType)도 정상 변환한다")
        void shouldConvertSkippedSyncResponse() {
            // Given
            ManualSyncTriggerResponse appResponse =
                    ManualSyncTriggerResponse.skipped(77L, "이미 동기화 요청이 진행 중입니다.");

            // When
            ManualSyncTriggerApiResponse result = mapper.toApiResponse(appResponse);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(77L);
            assertThat(result.syncOutboxId()).isNull();
            assertThat(result.syncType()).isNull();
            assertThat(result.message()).isEqualTo("이미 동기화 요청이 진행 중입니다.");
        }

        @Test
        @DisplayName("success() 팩토리 메서드로 생성된 응답을 정상 변환한다")
        void shouldConvertSuccessFactoryResponse() {
            // Given
            ManualSyncTriggerResponse appResponse =
                    ManualSyncTriggerResponse.success(10L, 500L, "CREATE");

            // When
            ManualSyncTriggerApiResponse result = mapper.toApiResponse(appResponse);

            // Then
            assertThat(result.crawledProductId()).isEqualTo(10L);
            assertThat(result.syncOutboxId()).isEqualTo(500L);
            assertThat(result.syncType()).isEqualTo("CREATE");
            assertThat(result.message()).isEqualTo("동기화 요청이 등록되었습니다.");
        }
    }
}
