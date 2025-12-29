package com.ryuqq.crawlinghub.application.product.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledRawPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledRawManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledRawManager 테스트")
class CrawledRawTransactionManagerTest {

    @Mock private CrawledRawPersistencePort crawledRawPersistencePort;

    @InjectMocks private CrawledRawTransactionManager manager;

    @Nested
    @DisplayName("save() 테스트")
    class Save {

        @Test
        @DisplayName("[성공] CrawledRaw 단건 저장")
        void shouldSaveCrawledRaw() {
            // Given
            CrawledRaw crawledRaw = createPendingRaw(1L, 100L, 12345L);
            CrawledRawId expectedId = CrawledRawId.of(1L);

            given(crawledRawPersistencePort.persist(crawledRaw)).willReturn(expectedId);

            // When
            CrawledRawId result = manager.save(crawledRaw);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawledRawPersistencePort).persist(crawledRaw);
        }
    }

    @Nested
    @DisplayName("saveAll() 테스트")
    class SaveAll {

        @Test
        @DisplayName("[성공] CrawledRaw 벌크 저장")
        void shouldSaveAllCrawledRaws() {
            // Given
            List<CrawledRaw> raws =
                    List.of(
                            createPendingRaw(1L, 100L, 12345L),
                            createPendingRaw(1L, 100L, 12346L),
                            createPendingRaw(1L, 100L, 12347L));
            List<CrawledRawId> expectedIds =
                    List.of(CrawledRawId.of(1L), CrawledRawId.of(2L), CrawledRawId.of(3L));

            given(crawledRawPersistencePort.persistAll(raws)).willReturn(expectedIds);

            // When
            List<CrawledRawId> result = manager.saveAll(raws);

            // Then
            assertThat(result).hasSize(3);
            verify(crawledRawPersistencePort).persistAll(raws);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyInput() {
            // Given
            List<CrawledRaw> emptyList = List.of();

            // When
            List<CrawledRawId> result = manager.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] null 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForNullInput() {
            // When
            List<CrawledRawId> result = manager.saveAll(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsProcessed() 테스트")
    class MarkAsProcessed {

        @Test
        @DisplayName("[성공] CrawledRaw 처리 완료 상태로 변경")
        void shouldMarkAsProcessed() {
            // Given
            CrawledRaw crawledRaw = createPendingRaw(1L, 100L, 12345L);
            CrawledRawId expectedId = CrawledRawId.of(1L);

            given(crawledRawPersistencePort.persist(crawledRaw.markAsProcessed()))
                    .willReturn(expectedId);

            // When
            CrawledRawId result = manager.markAsProcessed(crawledRaw);

            // Then
            assertThat(result).isEqualTo(expectedId);
        }
    }

    @Nested
    @DisplayName("markAsFailed() 테스트")
    class MarkAsFailed {

        @Test
        @DisplayName("[성공] CrawledRaw 처리 실패 상태로 변경")
        void shouldMarkAsFailed() {
            // Given
            CrawledRaw crawledRaw = createPendingRaw(1L, 100L, 12345L);
            String errorMessage = "JSON 파싱 실패";
            CrawledRawId expectedId = CrawledRawId.of(1L);

            given(crawledRawPersistencePort.persist(crawledRaw.markAsFailed(errorMessage)))
                    .willReturn(expectedId);

            // When
            CrawledRawId result = manager.markAsFailed(crawledRaw, errorMessage);

            // Then
            assertThat(result).isEqualTo(expectedId);
        }
    }

    // === Helper Methods ===

    private CrawledRaw createPendingRaw(long schedulerId, long sellerId, long itemNo) {
        return CrawledRaw.create(
                schedulerId, sellerId, itemNo, CrawlType.MINI_SHOP, "{\"test\": \"data\"}");
    }
}
