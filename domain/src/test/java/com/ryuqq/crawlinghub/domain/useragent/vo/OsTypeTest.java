package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("OsType 단위 테스트")
class OsTypeTest {

    @Nested
    @DisplayName("parseFrom() 테스트")
    class ParseFromTest {

        @Test
        @DisplayName("iPhone UA에서 iOS를 파싱한다")
        void parsesIphoneAsIos() {
            OsType result =
                    OsType.parseFrom("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)");
            assertThat(result).isEqualTo(OsType.IOS);
        }

        @Test
        @DisplayName("iPad UA에서 iOS를 파싱한다")
        void parsesIpadAsIos() {
            OsType result = OsType.parseFrom("Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)");
            assertThat(result).isEqualTo(OsType.IOS);
        }

        @Test
        @DisplayName("Android UA에서 Android를 파싱한다")
        void parsesAndroid() {
            OsType result = OsType.parseFrom("Mozilla/5.0 (Linux; Android 10; SM-G973F)");
            assertThat(result).isEqualTo(OsType.ANDROID);
        }

        @Test
        @DisplayName("Windows UA에서 Windows를 파싱한다")
        void parsesWindows() {
            OsType result = OsType.parseFrom("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(result).isEqualTo(OsType.WINDOWS);
        }

        @Test
        @DisplayName("Mac OS X UA에서 macOS를 파싱한다")
        void parsesMacOs() {
            OsType result = OsType.parseFrom("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
            assertThat(result).isEqualTo(OsType.MACOS);
        }

        @Test
        @DisplayName("CrOS UA에서 Chrome OS를 파싱한다")
        void parsesChromeOs() {
            OsType result = OsType.parseFrom("Mozilla/5.0 (X11; CrOS x86_64 14541.0.0)");
            assertThat(result).isEqualTo(OsType.CHROME_OS);
        }

        @Test
        @DisplayName("null UA는 Linux로 파싱한다")
        void parsesNullAsLinux() {
            OsType result = OsType.parseFrom(null);
            assertThat(result).isEqualTo(OsType.LINUX);
        }

        @Test
        @DisplayName("빈 UA는 Linux로 파싱한다")
        void parsesBlankAsLinux() {
            OsType result = OsType.parseFrom("  ");
            assertThat(result).isEqualTo(OsType.LINUX);
        }
    }

    @Nested
    @DisplayName("extractVersion() 테스트")
    class ExtractVersionTest {

        @Test
        @DisplayName("Windows NT 버전을 추출한다")
        void extractsWindowsVersion() {
            String version =
                    OsType.WINDOWS.extractVersion("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(version).isEqualTo("10.0");
        }

        @Test
        @DisplayName("macOS 버전을 추출한다")
        void extractsMacOsVersion() {
            String version =
                    OsType.MACOS.extractVersion("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
            assertThat(version).isEqualTo("10.15.7");
        }

        @Test
        @DisplayName("iOS 버전을 추출한다")
        void extractsIosVersion() {
            String version =
                    OsType.IOS.extractVersion(
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)");
            assertThat(version).isEqualTo("14.0");
        }

        @Test
        @DisplayName("Android 버전을 추출한다")
        void extractsAndroidVersion() {
            String version =
                    OsType.ANDROID.extractVersion("Mozilla/5.0 (Linux; Android 10; SM-G973F)");
            assertThat(version).isEqualTo("10");
        }

        @Test
        @DisplayName("Linux는 버전 패턴이 없으므로 null을 반환한다")
        void returnsNullForLinux() {
            String version = OsType.LINUX.extractVersion("Mozilla/5.0 (X11; Linux x86_64)");
            assertThat(version).isNull();
        }
    }

    @Nested
    @DisplayName("getDisplayName() 테스트")
    class GetDisplayNameTest {

        @Test
        @DisplayName("각 OsType의 표시 이름을 반환한다")
        void returnsDisplayNames() {
            assertThat(OsType.WINDOWS.getDisplayName()).isEqualTo("Windows");
            assertThat(OsType.MACOS.getDisplayName()).isEqualTo("macOS");
            assertThat(OsType.LINUX.getDisplayName()).isEqualTo("Linux");
            assertThat(OsType.IOS.getDisplayName()).isEqualTo("iOS");
            assertThat(OsType.ANDROID.getDisplayName()).isEqualTo("Android");
            assertThat(OsType.CHROME_OS.getDisplayName()).isEqualTo("Chrome OS");
        }
    }
}
