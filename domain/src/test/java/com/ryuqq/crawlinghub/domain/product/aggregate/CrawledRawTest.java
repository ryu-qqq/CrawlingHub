package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("CrawledRaw Aggregate 단위 테스트")
class CrawledRawTest {

    private static final Instant NOW = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("유효한 파라미터로 신규 CrawledRaw를 생성한다")
        void createNewCrawledRaw() {
            // when
            CrawledRaw raw =
                    CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.MINI_SHOP, "{\"items\":[]}", NOW);

            // then
            assertThat(raw.getCrawlSchedulerId()).isEqualTo(1L);
            assertThat(raw.getSellerId()).isEqualTo(100L);
            assertThat(raw.getItemNo()).isEqualTo(12345L);
            assertThat(raw.getCrawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(raw.getRawData()).isEqualTo("{\"items\":[]}");
            assertThat(raw.getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(raw.getErrorMessage()).isNull();
            assertThat(raw.getCreatedAt()).isEqualTo(NOW);
            assertThat(raw.getProcessedAt()).isNull();
            assertThat(raw.isPending()).isTrue();
        }

        @Test
        @DisplayName("crawlType이 null이면 예외가 발생한다")
        void throwWhenCrawlTypeIsNull() {
            assertThatThrownBy(() -> CrawledRaw.forNew(1L, 100L, 12345L, null, "{}", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("crawlType");
        }

        @Test
        @DisplayName("rawData가 null이면 예외가 발생한다")
        void throwWhenRawDataIsNull() {
            assertThatThrownBy(
                            () ->
                                    CrawledRaw.forNew(
                                            1L, 100L, 12345L, CrawlType.MINI_SHOP, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rawData");
        }

        @Test
        @DisplayName("rawData가 빈 문자열이면 예외가 발생한다")
        void throwWhenRawDataIsBlank() {
            assertThatThrownBy(
                            () ->
                                    CrawledRaw.forNew(
                                            1L, 100L, 12345L, CrawlType.MINI_SHOP, "   ", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rawData");
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 데이터로 CrawledRaw를 복원한다")
        void reconstructCrawledRaw() {
            // when
            CrawledRaw raw =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            2L,
                            200L,
                            99999L,
                            CrawlType.DETAIL,
                            "{\"detail\":\"data\"}",
                            RawDataStatus.PROCESSED,
                            null,
                            NOW,
                            NOW.plusSeconds(60));

            // then
            assertThat(raw.getIdValue()).isEqualTo(1L);
            assertThat(raw.getCrawlType()).isEqualTo(CrawlType.DETAIL);
            assertThat(raw.getStatus()).isEqualTo(RawDataStatus.PROCESSED);
            assertThat(raw.isProcessed()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 전환 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("markAsProcessed - PROCESSED 상태의 새 CrawledRaw를 반환한다")
        void markAsProcessed() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.MINI_SHOP, "{}", NOW);

            // when
            CrawledRaw processed = raw.markAsProcessed(NOW);

            // then
            assertThat(processed.isProcessed()).isTrue();
            assertThat(processed.getProcessedAt()).isEqualTo(NOW);
            assertThat(processed.getErrorMessage()).isNull();
            // 불변성 확인 - 원본은 변경되지 않음
            assertThat(raw.isPending()).isTrue();
        }

        @Test
        @DisplayName("markAsFailed - FAILED 상태의 새 CrawledRaw를 반환한다")
        void markAsFailed() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.MINI_SHOP, "{}", NOW);

            // when
            CrawledRaw failed = raw.markAsFailed("Parse error", NOW);

            // then
            assertThat(failed.isFailed()).isTrue();
            assertThat(failed.getErrorMessage()).isEqualTo("Parse error");
            assertThat(failed.getProcessedAt()).isEqualTo(NOW);
            // 불변성 확인 - 원본은 변경되지 않음
            assertThat(raw.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("타입 확인 메서드 테스트")
    class TypeCheckTest {

        @Test
        @DisplayName("isMiniShop - MINI_SHOP 타입이면 true")
        void isMiniShop() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.MINI_SHOP, "{}", NOW);

            // then
            assertThat(raw.isMiniShop()).isTrue();
            assertThat(raw.isDetail()).isFalse();
            assertThat(raw.isOption()).isFalse();
        }

        @Test
        @DisplayName("isDetail - DETAIL 타입이면 true")
        void isDetail() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.DETAIL, "{}", NOW);

            // then
            assertThat(raw.isDetail()).isTrue();
            assertThat(raw.isMiniShop()).isFalse();
        }

        @Test
        @DisplayName("isOption - OPTION 타입이면 true")
        void isOption() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.OPTION, "{}", NOW);

            // then
            assertThat(raw.isOption()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID이면 동일하다")
        void sameIdAreEqual() {
            // given
            CrawledRaw raw1 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            1L,
                            100L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            NOW,
                            null);
            CrawledRaw raw2 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            2L,
                            200L,
                            99999L,
                            CrawlType.DETAIL,
                            "{other}",
                            RawDataStatus.PROCESSED,
                            null,
                            NOW,
                            null);

            // then
            assertThat(raw1).isEqualTo(raw2);
            assertThat(raw1.hashCode()).isEqualTo(raw2.hashCode());
        }

        @Test
        @DisplayName("다른 ID이면 다르다")
        void differentIdAreNotEqual() {
            // given
            CrawledRaw raw1 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            1L,
                            100L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            NOW,
                            null);
            CrawledRaw raw2 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            1L,
                            100L,
                            12345L,
                            CrawlType.MINI_SHOP,
                            "{}",
                            RawDataStatus.PENDING,
                            null,
                            NOW,
                            null);

            // then
            assertThat(raw1).isNotEqualTo(raw2);
        }

        @Test
        @DisplayName("getIdValue() - ID가 없으면 null을 반환한다")
        void getIdValueNullWhenNoId() {
            // given
            CrawledRaw raw = CrawledRaw.forNew(1L, 100L, 12345L, CrawlType.MINI_SHOP, "{}", NOW);

            // then
            assertThat(raw.getIdValue()).isNull();
        }
    }
}
