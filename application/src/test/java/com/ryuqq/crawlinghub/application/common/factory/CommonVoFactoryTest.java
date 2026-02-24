package com.ryuqq.crawlinghub.application.common.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.DateRange;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CommonVoFactory 단위 테스트
 *
 * <p>Domain VO 생성 위임 팩토리 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CommonVoFactory 테스트")
class CommonVoFactoryTest {

    private CommonVoFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CommonVoFactory();
    }

    @Nested
    @DisplayName("createDateRange() 테스트")
    class CreateDateRange {

        @Test
        @DisplayName("[성공] 날짜 범위 VO 생성")
        void shouldCreateDateRange() {
            // Given
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);

            // When
            DateRange range = factory.createDateRange(start, end);

            // Then
            assertThat(range).isNotNull();
            assertThat(range.startDate()).isEqualTo(start);
            assertThat(range.endDate()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("createPageRequest() 테스트")
    class CreatePageRequest {

        @Test
        @DisplayName("[성공] 페이지 요청 VO 생성")
        void shouldCreatePageRequest() {
            // Given
            int page = 0;
            int size = 20;

            // When
            PageRequest request = factory.createPageRequest(page, size);

            // Then
            assertThat(request).isNotNull();
            assertThat(request.page()).isEqualTo(page);
            assertThat(request.size()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("parseSortDirection() 테스트")
    class ParseSortDirection {

        @Test
        @DisplayName("[성공] ASC 정렬 방향 파싱")
        void shouldParseAscSortDirection() {
            // When
            SortDirection direction = factory.parseSortDirection("ASC");

            // Then
            assertThat(direction).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("[성공] DESC 정렬 방향 파싱")
        void shouldParseDescSortDirection() {
            // When
            SortDirection direction = factory.parseSortDirection("DESC");

            // Then
            assertThat(direction).isEqualTo(SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("createQueryContext() 테스트")
    class CreateQueryContext {

        @Test
        @DisplayName("[성공] QueryContext 생성 (기본)")
        void shouldCreateQueryContext() {
            // Given
            SortDirection direction = SortDirection.DESC;
            PageRequest pageRequest = PageRequest.of(0, 20);

            // When
            QueryContext<TestSortKey> context =
                    factory.createQueryContext(TestSortKey.NAME, direction, pageRequest);

            // Then
            assertThat(context).isNotNull();
            assertThat(context.sortKey()).isEqualTo(TestSortKey.NAME);
            assertThat(context.sortDirection()).isEqualTo(direction);
        }

        @Test
        @DisplayName("[성공] QueryContext 생성 (includeDeleted 포함)")
        void shouldCreateQueryContextWithIncludeDeleted() {
            // Given
            SortDirection direction = SortDirection.ASC;
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            QueryContext<TestSortKey> context =
                    factory.createQueryContext(TestSortKey.NAME, direction, pageRequest, true);

            // Then
            assertThat(context).isNotNull();
            assertThat(context.includeDeleted()).isTrue();
        }
    }

    /** 테스트용 SortKey 구현체 */
    enum TestSortKey implements com.ryuqq.crawlinghub.domain.common.vo.SortKey {
        NAME;

        @Override
        public String fieldName() {
            return "name";
        }
    }
}
