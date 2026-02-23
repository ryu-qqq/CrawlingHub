package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledRawJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawledRaw 특수 사항:
 *
 * <ul>
 *   <li>Instant 타입 시간 필드 (Entity와 Domain 동일)
 *   <li>CrawlType, RawDataStatus Enum 변환
 *   <li>rawData는 JSON 형식 문자열
 *   <li>processedAt은 PENDING 상태에서 null
 *   <li>errorMessage는 FAILED 상태에서만 값이 있음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawledRawJpaEntityMapper 단위 테스트")
class CrawledRawJpaEntityMapperTest {

    private CrawledRawJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawledRawJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - PENDING 상태 MINI_SHOP 타입 변환")
        void shouldConvertPendingMiniShopRawToEntity() {
            // Given
            Instant now = Instant.now();
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{\"itemName\": \"테스트 상품\", \"price\": 10000}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCrawlSchedulerId()).isEqualTo(100L);
            assertThat(entity.getSellerId()).isEqualTo(200L);
            assertThat(entity.getItemNo()).isEqualTo(12345L);
            assertThat(entity.getCrawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(entity.getRawData())
                    .isEqualTo("{\"itemName\": \"테스트 상품\", \"price\": 10000}");
            assertThat(entity.getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - PROCESSED 상태 DETAIL 타입 변환")
        void shouldConvertProcessedDetailRawToEntity() {
            // Given
            Instant createdAt = Instant.now().minus(5, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now();
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.DETAIL,
                            "{\"category\": \"패션\", \"description\": \"상세 설명\"}",
                            RawDataStatus.PROCESSED,
                            null,
                            createdAt,
                            processedAt);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getCrawlType()).isEqualTo(CrawlType.DETAIL);
            assertThat(entity.getStatus()).isEqualTo(RawDataStatus.PROCESSED);
            assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
            assertThat(entity.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("성공 - FAILED 상태 OPTION 타입 변환 (에러 메시지 포함)")
        void shouldConvertFailedOptionRawToEntity() {
            // Given
            Instant createdAt = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now().minus(5, ChronoUnit.MINUTES);
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(3L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.OPTION,
                            "{\"options\": []}",
                            RawDataStatus.FAILED,
                            "JSON 파싱 실패: 잘못된 형식",
                            createdAt,
                            processedAt);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getCrawlType()).isEqualTo(CrawlType.OPTION);
            assertThat(entity.getStatus()).isEqualTo(RawDataStatus.FAILED);
            assertThat(entity.getErrorMessage()).isEqualTo("JSON 파싱 실패: 잘못된 형식");
            assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("성공 - 신규 Raw 변환 (ID null)")
        void shouldConvertNewRawToEntity() {
            // Given
            Instant now = Instant.now();
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.forNew(),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{\"test\": \"data\"}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getCrawlSchedulerId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - PENDING Entity를 Domain으로 변환")
        void shouldConvertEntityToPendingDomain() {
            // Given
            Instant now = Instant.now();
            CrawledRawJpaEntity entity =
                    CrawledRawJpaEntity.of(
                            1L,
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{\"itemName\": \"테스트 상품\"}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When
            CrawledRaw domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getIdValue()).isEqualTo(1L);
            assertThat(domain.getCrawlSchedulerId()).isEqualTo(100L);
            assertThat(domain.getSellerId()).isEqualTo(200L);
            assertThat(domain.getItemNo()).isEqualTo(12345L);
            assertThat(domain.getCrawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(domain.getRawData()).isEqualTo("{\"itemName\": \"테스트 상품\"}");
            assertThat(domain.getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(domain.getErrorMessage()).isNull();
            assertThat(domain.getCreatedAt()).isEqualTo(now);
            assertThat(domain.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - PROCESSED Entity를 Domain으로 변환")
        void shouldConvertEntityToProcessedDomain() {
            // Given
            Instant createdAt = Instant.now().minus(5, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now();
            CrawledRawJpaEntity entity =
                    CrawledRawJpaEntity.of(
                            2L,
                            100L,
                            200L,
                            12345L,
                            CrawlType.DETAIL,
                            "{\"category\": \"패션\"}",
                            RawDataStatus.PROCESSED,
                            null,
                            createdAt,
                            processedAt);

            // When
            CrawledRaw domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(RawDataStatus.PROCESSED);
            assertThat(domain.getProcessedAt()).isEqualTo(processedAt);
            assertThat(domain.isProcessed()).isTrue();
        }

        @Test
        @DisplayName("성공 - FAILED Entity를 Domain으로 변환")
        void shouldConvertEntityToFailedDomain() {
            // Given
            Instant createdAt = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now();
            CrawledRawJpaEntity entity =
                    CrawledRawJpaEntity.of(
                            3L,
                            100L,
                            200L,
                            12345L,
                            CrawlType.OPTION,
                            "{\"options\": []}",
                            RawDataStatus.FAILED,
                            "처리 중 오류 발생",
                            createdAt,
                            processedAt);

            // When
            CrawledRaw domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(RawDataStatus.FAILED);
            assertThat(domain.getErrorMessage()).isEqualTo("처리 중 오류 발생");
            assertThat(domain.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - PENDING Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForPendingRaw() {
            // Given
            Instant now = Instant.now();
            CrawledRaw original =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{\"test\": \"data\"}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(original);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getIdValue()).isEqualTo(original.getIdValue());
            assertThat(restored.getCrawlSchedulerId()).isEqualTo(original.getCrawlSchedulerId());
            assertThat(restored.getSellerId()).isEqualTo(original.getSellerId());
            assertThat(restored.getItemNo()).isEqualTo(original.getItemNo());
            assertThat(restored.getCrawlType()).isEqualTo(original.getCrawlType());
            assertThat(restored.getRawData()).isEqualTo(original.getRawData());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getErrorMessage()).isEqualTo(original.getErrorMessage());
        }

        @Test
        @DisplayName("성공 - PROCESSED Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForProcessedRaw() {
            // Given
            Instant createdAt = Instant.now().minus(5, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now();
            CrawledRaw original =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.DETAIL,
                            "{\"category\": \"패션\"}",
                            RawDataStatus.PROCESSED,
                            null,
                            createdAt,
                            processedAt);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(original);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getStatus()).isEqualTo(RawDataStatus.PROCESSED);
            assertThat(restored.getProcessedAt()).isEqualTo(original.getProcessedAt());
        }

        @Test
        @DisplayName("성공 - 다양한 CrawlType의 양방향 변환")
        void shouldMaintainConsistencyForVariousCrawlTypes() {
            // Given
            Instant now = Instant.now();
            CrawledRaw miniShop =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);
            CrawledRaw detail =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.DETAIL,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);
            CrawledRaw option =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(3L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.OPTION,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When & Then - MINI_SHOP
            CrawledRaw restoredMiniShop = mapper.toDomain(mapper.toEntity(miniShop));
            assertThat(restoredMiniShop.getCrawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(restoredMiniShop.isMiniShop()).isTrue();

            // When & Then - DETAIL
            CrawledRaw restoredDetail = mapper.toDomain(mapper.toEntity(detail));
            assertThat(restoredDetail.getCrawlType()).isEqualTo(CrawlType.DETAIL);
            assertThat(restoredDetail.isDetail()).isTrue();

            // When & Then - OPTION
            CrawledRaw restoredOption = mapper.toDomain(mapper.toEntity(option));
            assertThat(restoredOption.getCrawlType()).isEqualTo(CrawlType.OPTION);
            assertThat(restoredOption.isOption()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다양한 RawDataStatus의 양방향 변환")
        void shouldMaintainConsistencyForVariousStatuses() {
            // Given
            Instant now = Instant.now();
            CrawledRaw pending =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);
            CrawledRaw processed =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PROCESSED,
                            null,
                            now,
                            now);
            CrawledRaw failed =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(3L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.FAILED,
                            "Error message",
                            now,
                            now);

            // When & Then - PENDING
            CrawledRaw restoredPending = mapper.toDomain(mapper.toEntity(pending));
            assertThat(restoredPending.getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(restoredPending.isPending()).isTrue();

            // When & Then - PROCESSED
            CrawledRaw restoredProcessed = mapper.toDomain(mapper.toEntity(processed));
            assertThat(restoredProcessed.getStatus()).isEqualTo(RawDataStatus.PROCESSED);
            assertThat(restoredProcessed.isProcessed()).isTrue();

            // When & Then - FAILED
            CrawledRaw restoredFailed = mapper.toDomain(mapper.toEntity(failed));
            assertThat(restoredFailed.getStatus()).isEqualTo(RawDataStatus.FAILED);
            assertThat(restoredFailed.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant 시간 필드 변환 (변환 없이 직접 저장)")
        void shouldPreserveInstantFields() {
            // Given
            Instant createdAt = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant processedAt = Instant.now();
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PROCESSED,
                            null,
                            createdAt,
                            processedAt);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then - Instant는 변환 없이 직접 저장되므로 정확히 일치해야 함
            assertThat(restored.getCreatedAt()).isEqualTo(domain.getCreatedAt());
            assertThat(restored.getProcessedAt()).isEqualTo(domain.getProcessedAt());
        }

        @Test
        @DisplayName("성공 - null processedAt 처리")
        void shouldHandleNullProcessedAt() {
            // Given
            Instant now = Instant.now();
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getProcessedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Raw Data 처리")
    class RawDataTests {

        @Test
        @DisplayName("성공 - JSON 형식 rawData 변환")
        void shouldPreserveJsonRawData() {
            // Given
            String jsonData =
                    "{\"itemName\": \"테스트 상품\", \"price\": 10000, "
                            + "\"options\": [{\"color\": \"빨강\", \"size\": \"M\"}]}";
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            jsonData,
                            RawDataStatus.PENDING,
                            null,
                            Instant.now(),
                            null);

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getRawData()).isEqualTo(jsonData);
        }

        @Test
        @DisplayName("성공 - 긴 에러 메시지 변환")
        void shouldPreserveLongErrorMessage() {
            // Given
            String longErrorMessage =
                    "JSON 파싱 실패: 예상치 못한 토큰이 발견되었습니다. "
                            + "위치: line 5, column 23. 입력된 데이터가 올바른 JSON 형식인지 확인해주세요.";
            CrawledRaw domain =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            100L,
                            200L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.FAILED,
                            longErrorMessage,
                            Instant.now(),
                            Instant.now());

            // When
            CrawledRawJpaEntity entity = mapper.toEntity(domain);
            CrawledRaw restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getErrorMessage()).isEqualTo(longErrorMessage);
        }
    }
}
