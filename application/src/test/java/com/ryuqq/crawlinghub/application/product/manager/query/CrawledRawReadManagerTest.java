package com.ryuqq.crawlinghub.application.product.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.product.manager.CrawledRawReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledRawQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
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
 * CrawledRawReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledRawReadManager 테스트")
class CrawledRawReadManagerTest {

    @Mock private CrawledRawQueryPort crawledRawQueryPort;

    private CrawledRawReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawledRawReadManager(crawledRawQueryPort);
    }

    @Nested
    @DisplayName("findPendingByType() 테스트")
    class FindPendingByType {

        @Test
        @DisplayName("[성공] PENDING 상태의 MINI_SHOP 타입 Raw 조회 위임")
        void shouldDelegateToQueryPortWithPendingStatus() {
            // Given
            CrawlType crawlType = CrawlType.MINI_SHOP;
            int limit = 100;
            CrawledRaw raw =
                    CrawledRaw.forNew(1L, 1L, 10001L, CrawlType.MINI_SHOP, "{}", Instant.now());
            List<CrawledRaw> expected = List.of(raw);

            given(crawledRawQueryPort.findByStatusAndType(RawDataStatus.PENDING, crawlType, limit))
                    .willReturn(expected);

            // When
            List<CrawledRaw> result = manager.findPendingByType(crawlType, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            then(crawledRawQueryPort)
                    .should()
                    .findByStatusAndType(RawDataStatus.PENDING, crawlType, limit);
        }

        @Test
        @DisplayName("[성공] 결과 없음 시 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoPendingRaws() {
            // Given
            given(
                            crawledRawQueryPort.findByStatusAndType(
                                    RawDataStatus.PENDING, CrawlType.DETAIL, 50))
                    .willReturn(List.of());

            // When
            List<CrawledRaw> result = manager.findPendingByType(CrawlType.DETAIL, 50);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
