package com.ryuqq.crawlinghub.domain.execution.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlContext 단위 테스트")
class CrawlContextTest {

    private static final long CRAWL_TASK_ID = 1L;
    private static final long SCHEDULER_ID = 10L;
    private static final long SELLER_ID = 100L;
    private static final CrawlTaskType TASK_TYPE = CrawlTaskType.MINI_SHOP;
    private static final String ENDPOINT = "https://example.com/api/products";
    private static final Long USER_AGENT_ID = 5L;
    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (compatible)";
    private static final String SESSION_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final String NID = "nid-cookie-value";
    private static final String MUSTIT_UID = "mustit_uid-cookie-value";

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 CrawlContext를 생성한다")
        void createWithAllFields() {
            // when
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);

            // then
            assertThat(context.crawlTaskId()).isEqualTo(CRAWL_TASK_ID);
            assertThat(context.schedulerId()).isEqualTo(SCHEDULER_ID);
            assertThat(context.sellerId()).isEqualTo(SELLER_ID);
            assertThat(context.taskType()).isEqualTo(TASK_TYPE);
            assertThat(context.endpoint()).isEqualTo(ENDPOINT);
            assertThat(context.userAgentId()).isEqualTo(USER_AGENT_ID);
            assertThat(context.userAgentValue()).isEqualTo(USER_AGENT_VALUE);
            assertThat(context.sessionToken()).isEqualTo(SESSION_TOKEN);
            assertThat(context.nid()).isEqualTo(NID);
            assertThat(context.mustitUid()).isEqualTo(MUSTIT_UID);
        }

        @Test
        @DisplayName("쿠키 없이 CrawlContext를 생성한다")
        void createWithoutCookies() {
            // when
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            null,
                            null);

            // then
            assertThat(context.nid()).isNull();
            assertThat(context.mustitUid()).isNull();
        }
    }

    @Nested
    @DisplayName("hasSearchCookies() 메서드 테스트")
    class HasSearchCookiesTest {

        @Test
        @DisplayName("nid와 mustitUid가 모두 있으면 true")
        void returnsTrueWhenBothCookiesPresent() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);

            // then
            assertThat(context.hasSearchCookies()).isTrue();
        }

        @Test
        @DisplayName("nid가 null이면 false")
        void returnsFalseWhenNidIsNull() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            null,
                            MUSTIT_UID);

            // then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("mustitUid가 null이면 false")
        void returnsFalseWhenMusitUidIsNull() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            null);

            // then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("nid가 빈 문자열이면 false")
        void returnsFalseWhenNidIsBlank() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            "   ",
                            MUSTIT_UID);

            // then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("mustitUid가 빈 문자열이면 false")
        void returnsFalseWhenMusitUidIsBlank() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            "");

            // then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("nid와 mustitUid가 모두 없으면 false")
        void returnsFalseWhenBothCookiesAbsent() {
            // given
            CrawlContext context =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            null,
                            null);

            // then
            assertThat(context.hasSearchCookies()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 필드로 생성하면 동일하다")
        void sameFieldsAreEqual() {
            // given
            CrawlContext context1 =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);
            CrawlContext context2 =
                    new CrawlContext(
                            CRAWL_TASK_ID,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);

            // then
            assertThat(context1).isEqualTo(context2);
            assertThat(context1.hashCode()).isEqualTo(context2.hashCode());
        }

        @Test
        @DisplayName("다른 crawlTaskId이면 다르다")
        void differentCrawlTaskIdAreNotEqual() {
            // given
            CrawlContext context1 =
                    new CrawlContext(
                            1L,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);
            CrawlContext context2 =
                    new CrawlContext(
                            2L,
                            SCHEDULER_ID,
                            SELLER_ID,
                            TASK_TYPE,
                            ENDPOINT,
                            USER_AGENT_ID,
                            USER_AGENT_VALUE,
                            SESSION_TOKEN,
                            NID,
                            MUSTIT_UID);

            // then
            assertThat(context1).isNotEqualTo(context2);
        }
    }
}
