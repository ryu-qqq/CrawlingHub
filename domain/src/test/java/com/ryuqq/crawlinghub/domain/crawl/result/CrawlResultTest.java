package com.ryuqq.crawlinghub.domain.crawl.result;

import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CrawlResult 테스트")
class CrawlResultTest {

    private static final TaskId TASK_ID = TaskId.of(100L);
    private static final TaskType TASK_TYPE = TaskType.MINI_SHOP;
    private static final MustItSellerId SELLER_ID = MustItSellerId.of(200L);
    private static final String RAW_DATA = "{\"products\":[{\"id\":1,\"name\":\"상품A\"}]}";
    private static final LocalDateTime CRAWLED_AT = LocalDateTime.of(2025, 11, 10, 14, 30, 0);

    @Nested
    @DisplayName("create() 팩토리 메서드 테스트")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("유효한 값으로 신규 CrawlResult 생성 성공")
        void shouldCreateNewCrawlResult() {
            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID,
                TASK_TYPE,
                SELLER_ID,
                RAW_DATA,
                CRAWLED_AT
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTaskId()).isEqualTo(TASK_ID);
            assertThat(result.getTaskType()).isEqualTo(TASK_TYPE);
            assertThat(result.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(result.getRawData()).isEqualTo(RAW_DATA);
            assertThat(result.getCrawledAt()).isEqualTo(CRAWLED_AT);
        }

        @Test
        @DisplayName("create()로 생성 시 ID는 null")
        void shouldHaveNullIdWhenCreated() {
            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then: 신규 생성 시 ID는 null (DB 저장 후 할당)
            assertThat(result.getIdValue()).isNull();
        }

        @Test
        @DisplayName("create()로 생성 시 createdAt은 현재 시각")
        void shouldSetCreatedAtToNowWhenCreated() {
            // Given
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(result.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Task ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenTaskIdIsNull(TaskId nullTaskId) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                nullTaskId, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Task 타입이 null이면 예외 발생")
        void shouldThrowExceptionWhenTaskTypeIsNull(TaskType nullTaskType) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                TASK_ID, nullTaskType, SELLER_ID, RAW_DATA, CRAWLED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task 타입은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Seller ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull(MustItSellerId nullSellerId) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                TASK_ID, TASK_TYPE, nullSellerId, RAW_DATA, CRAWLED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Seller ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Raw 데이터가 null이면 예외 발생")
        void shouldThrowExceptionWhenRawDataIsNull(String nullRawData) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, nullRawData, CRAWLED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("크롤링 데이터는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Raw 데이터가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenRawDataIsBlank(String blankRawData) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, blankRawData, CRAWLED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("크롤링 데이터는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("크롤링 시각이 null이면 예외 발생")
        void shouldThrowExceptionWhenCrawledAtIsNull(LocalDateTime nullCrawledAt) {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, nullCrawledAt
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("크롤링 시각은 필수입니다");
        }

        @ParameterizedTest
        @EnumSource(TaskType.class)
        @DisplayName("모든 Task 타입으로 생성 가능")
        void shouldCreateWithAllTaskTypes(TaskType taskType) {
            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID, taskType, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then
            assertThat(result.getTaskType()).isEqualTo(taskType);
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteFactoryMethodTests {

        private static final CrawlResultId RESULT_ID = CrawlResultId.of(999L);
        private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 11, 10, 10, 0, 0);

        @Test
        @DisplayName("유효한 값으로 DB reconstitute 성공")
        void shouldReconstituteFromDb() {
            // When
            CrawlResult result = CrawlResult.reconstitute(
                RESULT_ID,
                TASK_ID,
                TASK_TYPE,
                SELLER_ID,
                RAW_DATA,
                CRAWLED_AT,
                CREATED_AT
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdValue()).isEqualTo(999L);
            assertThat(result.getTaskId()).isEqualTo(TASK_ID);
            assertThat(result.getTaskType()).isEqualTo(TASK_TYPE);
            assertThat(result.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(result.getRawData()).isEqualTo(RAW_DATA);
            assertThat(result.getCrawledAt()).isEqualTo(CRAWLED_AT);
            assertThat(result.getCreatedAt()).isEqualTo(CREATED_AT);
        }

        @Test
        @DisplayName("reconstitute()는 ID가 필수")
        void shouldRequireIdForReconstitute() {
            // When & Then
            assertThatThrownBy(() -> CrawlResult.reconstitute(
                null, TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, CREATED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }

        @Test
        @DisplayName("reconstitute()로 생성 시 모든 필드 유지")
        void shouldPreserveAllFieldsWhenReconstituted() {
            // Given
            CrawlResultId id = CrawlResultId.of(1L);
            LocalDateTime specificCreatedAt = LocalDateTime.of(2025, 11, 1, 0, 0, 0);

            // When
            CrawlResult result = CrawlResult.reconstitute(
                id, TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, specificCreatedAt
            );

            // Then: DB에서 복원한 경우 createdAt도 정확히 유지됨
            assertThat(result.getCreatedAt()).isEqualTo(specificCreatedAt);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 Getter 테스트")
    class LawOfDemeterGetterTests {

        @Test
        @DisplayName("getIdValue()는 ID의 Long 값 반환 (Law of Demeter)")
        void shouldReturnIdValueDirectly() {
            // Given
            CrawlResultId id = CrawlResultId.of(123L);
            CrawlResult result = CrawlResult.reconstitute(
                id, TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, LocalDateTime.now()
            );

            // When
            Long idValue = result.getIdValue();

            // Then: result.getId().value() 체이닝 대신 getIdValue() 사용
            assertThat(idValue).isEqualTo(123L);
        }

        @Test
        @DisplayName("getTaskIdValue()는 Task ID의 Long 값 반환 (Law of Demeter)")
        void shouldReturnTaskIdValueDirectly() {
            // Given
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // When
            Long taskIdValue = result.getTaskIdValue();

            // Then: result.getTaskId().value() 체이닝 대신 getTaskIdValue() 사용
            assertThat(taskIdValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("getSellerIdValue()는 Seller ID의 Long 값 반환 (Law of Demeter)")
        void shouldReturnSellerIdValueDirectly() {
            // Given
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // When
            Long sellerIdValue = result.getSellerIdValue();

            // Then: result.getSellerId().value() 체이닝 대신 getSellerIdValue() 사용
            assertThat(sellerIdValue).isEqualTo(200L);
        }

        @Test
        @DisplayName("신규 생성 시 getIdValue()는 null 반환")
        void shouldReturnNullIdValueForNewResult() {
            // Given: create()로 생성 (ID 없음)
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // When
            Long idValue = result.getIdValue();

            // Then
            assertThat(idValue).isNull();
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 ID를 가진 CrawlResult는 동일하다")
        void shouldBeEqualWhenSameId() {
            // Given
            CrawlResultId sameId = CrawlResultId.of(123L);
            CrawlResult result1 = CrawlResult.reconstitute(
                sameId, TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, LocalDateTime.now()
            );
            CrawlResult result2 = CrawlResult.reconstitute(
                sameId,
                TaskId.of(999L), // 다른 taskId
                TaskType.PRODUCT_DETAIL, // 다른 taskType
                SELLER_ID,
                "{\"different\":\"data\"}", // 다른 rawData
                CRAWLED_AT,
                LocalDateTime.now()
            );

            // Then: ID만 같으면 동일하다고 판단 (Aggregate Identity)
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 CrawlResult는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentId() {
            // Given
            CrawlResult result1 = CrawlResult.reconstitute(
                CrawlResultId.of(1L), TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, LocalDateTime.now()
            );
            CrawlResult result2 = CrawlResult.reconstitute(
                CrawlResultId.of(2L), TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT, LocalDateTime.now()
            );

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("신규 생성된 CrawlResult는 ID가 null이므로 equals 비교 시 같다고 판단됨")
        void shouldHandleNullIdInEquals() {
            // Given: create()로 생성 (ID 없음)
            CrawlResult result1 = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );
            CrawlResult result2 = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then: ID가 null인 경우 Objects.equals(null, null) = true이므로 동일하다고 판단됨
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("자기 자신과 비교하면 동일하다")
        void shouldBeEqualToSelf() {
            // Given
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then
            assertThat(result).isEqualTo(result);
        }

        @Test
        @DisplayName("null과 비교하면 동일하지 않다")
        void shouldNotBeEqualToNull() {
            // Given
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // Then
            assertThat(result).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와 비교하면 동일하지 않다")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );
            String otherObject = "other";

            // Then
            assertThat(result).isNotEqualTo(otherObject);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드를 포함")
        void shouldIncludeKeyFieldsInToString() {
            // Given
            CrawlResult result = CrawlResult.reconstitute(
                CrawlResultId.of(123L),
                TASK_ID,
                TASK_TYPE,
                SELLER_ID,
                RAW_DATA,
                CRAWLED_AT,
                LocalDateTime.now()
            );

            // When
            String str = result.toString();

            // Then
            assertThat(str)
                .contains("CrawlResult")
                .contains("id=")
                .contains("taskId=")
                .contains("taskType=")
                .contains("sellerId=")
                .contains("crawledAt=")
                .contains("createdAt=");
        }

        @Test
        @DisplayName("toString()은 rawData를 포함하지 않는다")
        void shouldNotIncludeRawDataInToString() {
            // Given: 민감한 JSON 데이터
            String sensitiveData = "{\"password\":\"secret123\"}";
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, sensitiveData, CRAWLED_AT
            );

            // When
            String str = result.toString();

            // Then: 보안상 rawData는 toString에 포함하지 않음
            assertThat(str).doesNotContain(sensitiveData);
            assertThat(str).doesNotContain("rawData");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("META Task 결과 저장 시나리오")
        void shouldHandleMetaTaskResult() {
            // Given: META Task에서 전체 상품 개수를 크롤링
            String metaJson = "{\"totalCount\":12345}";

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID,
                TaskType.META,
                SELLER_ID,
                metaJson,
                LocalDateTime.now()
            );

            // Then
            assertThat(result.getTaskType()).isEqualTo(TaskType.META);
            assertThat(result.getRawData()).contains("totalCount");
        }

        @Test
        @DisplayName("MINI_SHOP Task 결과 저장 시나리오")
        void shouldHandleMiniShopTaskResult() {
            // Given: MINI_SHOP Task에서 상품 목록을 크롤링
            String miniShopJson = "[{\"id\":1,\"name\":\"상품A\",\"price\":10000}," +
                "{\"id\":2,\"name\":\"상품B\",\"price\":20000}]";

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID,
                TaskType.MINI_SHOP,
                SELLER_ID,
                miniShopJson,
                LocalDateTime.now()
            );

            // Then
            assertThat(result.getTaskType()).isEqualTo(TaskType.MINI_SHOP);
            assertThat(result.getRawData()).contains("상품A", "상품B");
        }

        @Test
        @DisplayName("PRODUCT_DETAIL Task 결과 저장 시나리오")
        void shouldHandleProductDetailTaskResult() {
            // Given: PRODUCT_DETAIL Task에서 상품 상세 정보를 크롤링
            String detailJson = "{\"id\":123,\"name\":\"갤럭시 S24\"," +
                "\"description\":\"최신형 스마트폰\",\"images\":[\"img1.jpg\"]}";

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID,
                TaskType.PRODUCT_DETAIL,
                SELLER_ID,
                detailJson,
                LocalDateTime.now()
            );

            // Then
            assertThat(result.getTaskType()).isEqualTo(TaskType.PRODUCT_DETAIL);
            assertThat(result.getRawData()).contains("갤럭시 S24", "images");
        }

        @Test
        @DisplayName("PRODUCT_OPTION Task 결과 저장 시나리오")
        void shouldHandleProductOptionTaskResult() {
            // Given: PRODUCT_OPTION Task에서 상품 옵션을 크롤링
            String optionJson = "{\"options\":[{\"color\":\"블랙\",\"size\":\"256GB\",\"stock\":10}," +
                "{\"color\":\"화이트\",\"size\":\"512GB\",\"stock\":5}]}";

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID,
                TaskType.PRODUCT_OPTION,
                SELLER_ID,
                optionJson,
                LocalDateTime.now()
            );

            // Then
            assertThat(result.getTaskType()).isEqualTo(TaskType.PRODUCT_OPTION);
            assertThat(result.getRawData()).contains("options", "stock");
        }

        @Test
        @DisplayName("DB 저장 후 조회 시나리오")
        void shouldHandleDbSaveAndLoad() {
            // Given: 신규 CrawlResult 생성 (ID 없음)
            CrawlResult newResult = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, RAW_DATA, CRAWLED_AT
            );

            // When: DB에 저장 후 ID가 할당되었다고 가정
            CrawlResultId assignedId = CrawlResultId.of(999L);
            CrawlResult loadedResult = CrawlResult.reconstitute(
                assignedId,
                newResult.getTaskId(),
                newResult.getTaskType(),
                newResult.getSellerId(),
                newResult.getRawData(),
                newResult.getCrawledAt(),
                newResult.getCreatedAt()
            );

            // Then: DB에서 조회한 결과는 ID를 가지고 있음
            assertThat(loadedResult.getIdValue()).isEqualTo(999L);
            assertThat(loadedResult.getRawData()).isEqualTo(newResult.getRawData());
        }

        @Test
        @DisplayName("복잡한 JSON 데이터도 저장 가능")
        void shouldHandleComplexJsonData() {
            // Given: 복잡한 JSON 구조
            String complexJson = """
                {
                    "products": [
                        {
                            "id": 1,
                            "name": "상품A",
                            "options": [
                                {"color": "빨강", "size": "L", "stock": 10},
                                {"color": "파랑", "size": "M", "stock": 5}
                            ],
                            "images": ["img1.jpg", "img2.jpg"]
                        }
                    ],
                    "totalCount": 100,
                    "timestamp": "2025-11-10T14:30:00"
                }
                """;

            // When
            CrawlResult result = CrawlResult.create(
                TASK_ID, TASK_TYPE, SELLER_ID, complexJson, CRAWLED_AT
            );

            // Then: 복잡한 JSON도 문제없이 저장됨
            assertThat(result.getRawData()).contains("products", "totalCount", "timestamp");
        }
    }
}
