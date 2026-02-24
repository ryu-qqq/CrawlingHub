package com.ryuqq.crawlinghub.application.product.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductCommandManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductCommandManager 단위 테스트")
class CrawledProductCommandManagerTest {

    @Mock private CrawledProductPersistencePort crawledProductPersistencePort;
    @Mock private CrawledProduct crawledProduct;

    @InjectMocks private CrawledProductCommandManager sut;

    @Nested
    @DisplayName("persist() 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("[성공] CrawledProduct 저장 후 ID 반환")
        void shouldPersistAndReturnId() {
            // Given
            CrawledProductId expectedId = CrawledProductId.of(1L);
            given(crawledProductPersistencePort.persist(crawledProduct)).willReturn(expectedId);

            // When
            CrawledProductId result = sut.persist(crawledProduct);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawledProductPersistencePort).persist(crawledProduct);
        }

        @Test
        @DisplayName("[성공] 신규 CrawledProduct 저장 시 PersistencePort에 위임")
        void shouldDelegatePersistToPersistencePort() {
            // Given
            CrawledProductId expectedId = CrawledProductId.of(99L);
            given(crawledProductPersistencePort.persist(crawledProduct)).willReturn(expectedId);

            // When
            CrawledProductId result = sut.persist(crawledProduct);

            // Then
            assertThat(result.value()).isEqualTo(99L);
            verify(crawledProductPersistencePort).persist(crawledProduct);
        }
    }
}
