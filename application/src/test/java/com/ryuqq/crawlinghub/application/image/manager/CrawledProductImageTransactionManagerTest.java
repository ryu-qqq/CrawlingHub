package com.ryuqq.crawlinghub.application.image.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductImagePersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductImageTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * <p><strong>주의</strong>: TransactionManager는 영속성 작업만 담당. 비즈니스 로직(domain method 호출)은 Service에서 처리.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductImageTransactionManager 테스트")
class CrawledProductImageTransactionManagerTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private CrawledProductImagePersistencePort imagePersistencePort;

    private CrawledProductImageTransactionManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawledProductImageTransactionManager(imagePersistencePort);
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] 이미지 단건 저장 (upsert)")
        void shouldPersistImage() {
            // Given
            CrawledProductImage image = createImage(null, 100L);
            CrawledProductImage savedImage = createImage(1L, 100L);
            given(imagePersistencePort.persist(image)).willReturn(savedImage);

            // When
            CrawledProductImage result = manager.persist(image);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            verify(imagePersistencePort).persist(image);
        }
    }

    @Nested
    @DisplayName("persistAll() 테스트")
    class PersistAll {

        @Test
        @DisplayName("[성공] 이미지 벌크 저장 (upsert)")
        void shouldPersistAllImages() {
            // Given
            List<CrawledProductImage> images =
                    List.of(createImage(null, 100L), createImage(null, 100L));
            List<CrawledProductImage> savedImages =
                    List.of(createImage(1L, 100L), createImage(2L, 100L));
            given(imagePersistencePort.persistAll(images)).willReturn(savedImages);

            // When
            List<CrawledProductImage> result = manager.persistAll(images);

            // Then
            assertThat(result).hasSize(2);
            verify(imagePersistencePort).persistAll(images);
        }

        @Test
        @DisplayName("[성공] 빈 목록 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyInput() {
            // Given
            List<CrawledProductImage> emptyList = List.of();

            // When
            List<CrawledProductImage> result = manager.persistAll(emptyList);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] null 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForNullInput() {
            // When
            List<CrawledProductImage> result = manager.persistAll(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // === Helper Methods ===

    private CrawledProductImage createImage(Long id, Long crawledProductId) {
        return CrawledProductImage.reconstitute(
                id,
                CrawledProductId.of(crawledProductId),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                1,
                null,
                null,
                FIXED_TIME,
                null);
    }
}
