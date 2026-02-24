package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("UserAgentMetadata Value Object 단위 테스트")
class UserAgentMetadataTest {

    @Nested
    @DisplayName("parseFrom() 팩토리 메서드 테스트")
    class ParseFromTest {

        @Test
        @DisplayName("iOS Safari UA를 파싱한다")
        void parsesIosSafariUserAgent() {
            String ua =
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) "
                            + "AppleWebKit/605.1.15 Version/14.0 Mobile/15E148 Safari/604.1";

            UserAgentMetadata metadata = UserAgentMetadata.parseFrom(ua);

            assertThat(metadata.getOsType()).isEqualTo(OsType.IOS);
            assertThat(metadata.getBrowserType()).isEqualTo(BrowserType.SAFARI);
            assertThat(metadata.getDeviceBrand()).isNotNull();
        }

        @Test
        @DisplayName("Windows Chrome UA를 파싱한다")
        void parsesWindowsChromeUserAgent() {
            String ua =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like"
                            + " Gecko) Chrome/91.0.4472.124 Safari/537.36";

            UserAgentMetadata metadata = UserAgentMetadata.parseFrom(ua);

            assertThat(metadata.getOsType()).isEqualTo(OsType.WINDOWS);
            assertThat(metadata.getBrowserType()).isEqualTo(BrowserType.CHROME);
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("명시적 값으로 생성한다")
        void createWithExplicitValues() {
            UserAgentMetadata metadata =
                    UserAgentMetadata.of(
                            DeviceBrand.IPHONE, OsType.IOS, "14.0", BrowserType.SAFARI, "14.0");

            assertThat(metadata.getDeviceBrand()).isEqualTo(DeviceBrand.IPHONE);
            assertThat(metadata.getOsType()).isEqualTo(OsType.IOS);
            assertThat(metadata.getOsVersion()).isEqualTo("14.0");
            assertThat(metadata.getBrowserType()).isEqualTo(BrowserType.SAFARI);
            assertThat(metadata.getBrowserVersion()).isEqualTo("14.0");
        }

        @Test
        @DisplayName("deviceBrand가 null이면 예외가 발생한다")
        void nullDeviceBrandThrowsException() {
            assertThatThrownBy(
                            () ->
                                    UserAgentMetadata.of(
                                            null, OsType.LINUX, null, BrowserType.CHROME, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("osType이 null이면 예외가 발생한다")
        void nullOsTypeThrowsException() {
            assertThatThrownBy(
                            () ->
                                    UserAgentMetadata.of(
                                            DeviceBrand.GENERIC,
                                            null,
                                            null,
                                            BrowserType.CHROME,
                                            null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("browserType이 null이면 예외가 발생한다")
        void nullBrowserTypeThrowsException() {
            assertThatThrownBy(
                            () ->
                                    UserAgentMetadata.of(
                                            DeviceBrand.GENERIC, OsType.LINUX, null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("defaultMetadata() 팩토리 메서드 테스트")
    class DefaultMetadataTest {

        @Test
        @DisplayName("기본 메타데이터를 생성한다")
        void createsDefaultMetadata() {
            UserAgentMetadata metadata = UserAgentMetadata.defaultMetadata();

            assertThat(metadata.getDeviceBrand()).isEqualTo(DeviceBrand.GENERIC);
            assertThat(metadata.getOsType()).isEqualTo(OsType.LINUX);
            assertThat(metadata.getOsVersion()).isNull();
            assertThat(metadata.getBrowserType()).isEqualTo(BrowserType.CHROME);
            assertThat(metadata.getBrowserVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드이면 동일하다")
        void sameFieldsAreEqual() {
            UserAgentMetadata meta1 =
                    UserAgentMetadata.of(
                            DeviceBrand.GENERIC, OsType.LINUX, null, BrowserType.CHROME, null);
            UserAgentMetadata meta2 =
                    UserAgentMetadata.of(
                            DeviceBrand.GENERIC, OsType.LINUX, null, BrowserType.CHROME, null);

            assertThat(meta1).isEqualTo(meta2);
            assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode());
        }

        @Test
        @DisplayName("다른 필드이면 다르다")
        void differentFieldsAreNotEqual() {
            UserAgentMetadata meta1 = UserAgentMetadata.defaultMetadata();
            UserAgentMetadata meta2 =
                    UserAgentMetadata.of(
                            DeviceBrand.IPHONE, OsType.IOS, "14.0", BrowserType.SAFARI, "14.0");

            assertThat(meta1).isNotEqualTo(meta2);
        }
    }
}
