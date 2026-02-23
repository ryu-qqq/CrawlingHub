package com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawlTask 특수 사항:
 *
 * <ul>
 *   <li>CrawlEndpoint → endpointBaseUrl, endpointPath, endpointQueryParams(JSON)
 *   <li>RetryCount VO → int 변환
 *   <li>Outbox는 별도 Entity로 관리 (toDomain에서 null)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlTaskJpaEntityMapper 단위 테스트")
class CrawlTaskJpaEntityMapperTest {

    private CrawlTaskJpaEntityMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new CrawlTaskJpaEntityMapper(objectMapper);
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - WAITING 상태 태스크 변환")
        void shouldConvertWaitingTaskToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getIdValue());
            assertThat(entity.getCrawlSchedulerId()).isEqualTo(domain.getCrawlSchedulerIdValue());
            assertThat(entity.getSellerId()).isEqualTo(domain.getSellerIdValue());
            assertThat(entity.getTaskType()).isEqualTo(domain.getTaskType());
            assertThat(entity.getStatus()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(entity.getRetryCount()).isEqualTo(domain.getRetryCountValue());
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - RUNNING 상태 태스크 변환")
        void shouldConvertRunningTaskToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aRunningTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);
        }

        @Test
        @DisplayName("성공 - SUCCESS 상태 태스크 변환")
        void shouldConvertSuccessTaskToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aSuccessTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlTaskStatus.SUCCESS);
        }

        @Test
        @DisplayName("성공 - FAILED 상태 태스크 변환 (최대 재시도)")
        void shouldConvertFailedTaskWithMaxRetryToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aFailedTaskWithMaxRetry();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
            assertThat(entity.getRetryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - RETRY 상태 태스크 변환")
        void shouldConvertRetryTaskToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aRetryTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlTaskStatus.RETRY);
            assertThat(entity.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - Endpoint 필드 변환")
        void shouldConvertEndpointFieldsToEntity() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getEndpointBaseUrl()).isEqualTo(domain.getEndpoint().baseUrl());
            assertThat(entity.getEndpointPath()).isEqualTo(domain.getEndpoint().path());
            // Query params should be serialized to JSON
            if (!domain.getEndpoint().queryParams().isEmpty()) {
                assertThat(entity.getEndpointQueryParams()).isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - WAITING Entity를 Domain으로 변환")
        void shouldConvertEntityToWaitingDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            "{\"page\":\"1\",\"size\":\"10\"}",
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getIdValue()).isEqualTo(1L);
            assertThat(domain.getCrawlSchedulerIdValue()).isEqualTo(100L);
            assertThat(domain.getSellerIdValue()).isEqualTo(200L);
            assertThat(domain.getTaskType()).isEqualTo(CrawlTaskType.MINI_SHOP);
            assertThat(domain.getStatus()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(domain.getRetryCountValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - RUNNING Entity를 Domain으로 변환")
        void shouldConvertEntityToRunningDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            2L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            null,
                            CrawlTaskStatus.RUNNING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);
        }

        @Test
        @DisplayName("성공 - Query Params JSON 역직렬화")
        void shouldDeserializeQueryParams() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            3L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            "{\"page\":\"1\",\"size\":\"10\",\"sort\":\"desc\"}",
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            Map<String, String> queryParams = domain.getEndpoint().queryParams();
            assertThat(queryParams).containsEntry("page", "1");
            assertThat(queryParams).containsEntry("size", "10");
            assertThat(queryParams).containsEntry("sort", "desc");
        }

        @Test
        @DisplayName("성공 - null Query Params 처리")
        void shouldHandleNullQueryParams() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            4L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            null,
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getEndpoint().queryParams()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 빈 문자열 Query Params 처리")
        void shouldHandleEmptyQueryParams() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            5L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            "   ",
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getEndpoint().queryParams()).isEmpty();
        }

        @Test
        @DisplayName("성공 - Outbox는 null로 설정됨")
        void shouldSetOutboxToNull() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            6L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            null,
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getOutbox()).isNull();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - WAITING Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForWaitingTask() {
            // Given
            CrawlTask original = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(original);
            CrawlTask restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getIdValue()).isEqualTo(original.getIdValue());
            assertThat(restored.getCrawlSchedulerIdValue())
                    .isEqualTo(original.getCrawlSchedulerIdValue());
            assertThat(restored.getSellerIdValue()).isEqualTo(original.getSellerIdValue());
            assertThat(restored.getTaskType()).isEqualTo(original.getTaskType());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getRetryCountValue()).isEqualTo(original.getRetryCountValue());
        }

        @Test
        @DisplayName("성공 - Endpoint 변환 일관성")
        void shouldMaintainEndpointConsistency() {
            // Given
            CrawlTask original = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(original);
            CrawlTask restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getEndpoint().baseUrl())
                    .isEqualTo(original.getEndpoint().baseUrl());
            assertThat(restored.getEndpoint().path()).isEqualTo(original.getEndpoint().path());
            assertThat(restored.getEndpoint().queryParams())
                    .isEqualTo(original.getEndpoint().queryParams());
        }

        @Test
        @DisplayName("성공 - 다양한 상태의 양방향 변환")
        void shouldMaintainConsistencyForVariousStatuses() {
            // Given
            CrawlTask running = CrawlTaskFixture.aRunningTask();
            CrawlTask success = CrawlTaskFixture.aSuccessTask();
            CrawlTask failed = CrawlTaskFixture.aFailedTask();

            // When & Then - Running
            CrawlTask restoredRunning = mapper.toDomain(mapper.toEntity(running));
            assertThat(restoredRunning.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);

            // When & Then - Success
            CrawlTask restoredSuccess = mapper.toDomain(mapper.toEntity(success));
            assertThat(restoredSuccess.getStatus()).isEqualTo(CrawlTaskStatus.SUCCESS);

            // When & Then - Failed
            CrawlTask restoredFailed = mapper.toDomain(mapper.toEntity(failed));
            assertThat(restoredFailed.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);
            CrawlTask restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getUpdatedAt())
                    .isCloseTo(
                            domain.getUpdatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("성공 - null 시간 필드 처리 (createdAt/updatedAt null)")
        void shouldHandleNullTimeFields() {
            // Given - createdAt/updatedAt이 null인 Entity
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.MINI_SHOP,
                            "https://api.example.com",
                            "/v1/products",
                            null,
                            CrawlTaskStatus.WAITING,
                            0,
                            null, // createdAt null
                            null); // updatedAt null

            // When
            CrawlTask domain = mapper.toDomain(entity);

            // Then - null 시간 필드가 허용되어야 함
            assertThat(domain).isNotNull();
            assertThat(domain.getCreatedAt()).isNull();
            assertThat(domain.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Query Params 직렬화/역직렬화")
    class QueryParamsSerializationTests {

        @Test
        @DisplayName("성공 - Query Params가 있는 경우 JSON으로 직렬화")
        void shouldSerializeQueryParamsToJson() {
            // Given - query params가 있는 Domain
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskJpaEntity entity = mapper.toEntity(domain);
            CrawlTask restored = mapper.toDomain(entity);

            // Then - query params가 동일하게 유지되어야 함
            assertThat(restored.getEndpoint().queryParams())
                    .isEqualTo(domain.getEndpoint().queryParams());
        }
    }
}
