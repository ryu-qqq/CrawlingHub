package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("BrowserType 단위 테스트")
class BrowserTypeTest {

    @Nested
    @DisplayName("parseFrom() 테스트")
    class ParseFromTest {

        @Test
        @DisplayName("Edge UA를 EDGE로 파싱한다")
        void parsesEdge() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/91.0"
                                    + " Edg/91.0.864.59");
            assertThat(result).isEqualTo(BrowserType.EDGE);
        }

        @Test
        @DisplayName("Opera UA를 OPERA로 파싱한다")
        void parsesOpera() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (Windows NT 10.0) Chrome/91.0 OPR/77.0.4054.172");
            assertThat(result).isEqualTo(BrowserType.OPERA);
        }

        @Test
        @DisplayName("Samsung Browser UA를 SAMSUNG_BROWSER로 파싱한다")
        void parsesSamsungBrowser() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (Linux; Android 11) SamsungBrowser/14.0 Chrome/87.0");
            assertThat(result).isEqualTo(BrowserType.SAMSUNG_BROWSER);
        }

        @Test
        @DisplayName("Firefox UA를 FIREFOX로 파싱한다")
        void parsesFirefox() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (Windows NT 10.0) Gecko/20100101 Firefox/89.0");
            assertThat(result).isEqualTo(BrowserType.FIREFOX);
        }

        @Test
        @DisplayName("Chrome UA를 CHROME으로 파싱한다")
        void parsesChrome() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/91.0"
                                    + " Safari/537.36");
            assertThat(result).isEqualTo(BrowserType.CHROME);
        }

        @Test
        @DisplayName("Safari UA를 SAFARI로 파싱한다")
        void parsesSafari() {
            BrowserType result =
                    BrowserType.parseFrom(
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0) Version/14.0 Mobile"
                                    + " Safari/604.1");
            assertThat(result).isEqualTo(BrowserType.SAFARI);
        }

        @Test
        @DisplayName("null UA는 CHROME으로 파싱한다")
        void parsesNullAsChrome() {
            BrowserType result = BrowserType.parseFrom(null);
            assertThat(result).isEqualTo(BrowserType.CHROME);
        }

        @Test
        @DisplayName("빈 UA는 CHROME으로 파싱한다")
        void parsesBlankAsChrome() {
            BrowserType result = BrowserType.parseFrom("  ");
            assertThat(result).isEqualTo(BrowserType.CHROME);
        }
    }

    @Nested
    @DisplayName("extractVersion() 테스트")
    class ExtractVersionTest {

        @Test
        @DisplayName("Chrome 버전을 추출한다")
        void extractsChromeVersion() {
            String version =
                    BrowserType.CHROME.extractVersion(
                            "Mozilla/5.0 Chrome/91.0.4472.124 Safari/537.36");
            assertThat(version).isEqualTo("91.0.4472.124");
        }

        @Test
        @DisplayName("Firefox 버전을 추출한다")
        void extractsFirefoxVersion() {
            String version =
                    BrowserType.FIREFOX.extractVersion(
                            "Mozilla/5.0 (Windows NT 10.0) Firefox/89.0");
            assertThat(version).isEqualTo("89.0");
        }

        @Test
        @DisplayName("버전이 없으면 null을 반환한다")
        void returnsNullWhenNoVersion() {
            String version = BrowserType.CHROME.extractVersion("Mozilla/5.0 (Windows NT 10.0)");
            assertThat(version).isNull();
        }
    }

    @Nested
    @DisplayName("getDisplayName() 테스트")
    class GetDisplayNameTest {

        @Test
        @DisplayName("각 BrowserType의 표시 이름을 반환한다")
        void returnsDisplayNames() {
            assertThat(BrowserType.CHROME.getDisplayName()).isEqualTo("Chrome");
            assertThat(BrowserType.SAFARI.getDisplayName()).isEqualTo("Safari");
            assertThat(BrowserType.FIREFOX.getDisplayName()).isEqualTo("Firefox");
            assertThat(BrowserType.EDGE.getDisplayName()).isEqualTo("Edge");
            assertThat(BrowserType.OPERA.getDisplayName()).isEqualTo("Opera");
            assertThat(BrowserType.SAMSUNG_BROWSER.getDisplayName()).isEqualTo("Samsung Browser");
        }
    }
}
