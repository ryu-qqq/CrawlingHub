package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("DeviceType Value Object 단위 테스트")
class DeviceTypeTest {

    @Nested
    @DisplayName("parse() 테스트")
    class ParseTest {

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)",
                    "Mozilla/5.0 (Linux; Android 10; Mobile)"
                })
        @DisplayName("Mobile 관련 UA 문자열을 MOBILE 타입으로 파싱한다")
        void parsesMobileUserAgent(String userAgent) {
            DeviceType deviceType = DeviceType.parse(userAgent);
            assertThat(deviceType.isMobile()).isTrue();
        }

        @Test
        @DisplayName("iPad UA를 TABLET 타입으로 파싱한다")
        void parsesIpadAsTablet() {
            String ua = "Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)";
            DeviceType deviceType = DeviceType.parse(ua);
            assertThat(deviceType.isTablet()).isTrue();
        }

        @Test
        @DisplayName("Android Tablet UA를 TABLET 타입으로 파싱한다")
        void parsesAndroidTabletAsTablet() {
            String ua = "Mozilla/5.0 (Linux; Android 10; Tablet)";
            DeviceType deviceType = DeviceType.parse(ua);
            assertThat(deviceType.isTablet()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
                    "Mozilla/5.0 (X11; Linux x86_64)"
                })
        @DisplayName("Desktop UA를 DESKTOP 타입으로 파싱한다")
        void parsesDesktopUserAgent(String userAgent) {
            DeviceType deviceType = DeviceType.parse(userAgent);
            assertThat(deviceType.isDesktop()).isTrue();
        }

        @Test
        @DisplayName("null UA는 DESKTOP 타입으로 파싱한다")
        void parsesNullAsDesktop() {
            DeviceType deviceType = DeviceType.parse(null);
            assertThat(deviceType.isDesktop()).isTrue();
        }

        @Test
        @DisplayName("빈 UA는 DESKTOP 타입으로 파싱한다")
        void parsesBlankAsDesktop() {
            DeviceType deviceType = DeviceType.parse("  ");
            assertThat(deviceType.isDesktop()).isTrue();
        }
    }

    @Nested
    @DisplayName("of(String) 팩토리 메서드 테스트")
    class OfStringFactoryTest {

        @Test
        @DisplayName("MOBILE 타입 이름으로 생성한다")
        void createMobileByTypeName() {
            DeviceType deviceType = DeviceType.of("MOBILE");
            assertThat(deviceType.isMobile()).isTrue();
        }

        @Test
        @DisplayName("대소문자 구분 없이 생성한다")
        void createCaseInsensitive() {
            DeviceType deviceType = DeviceType.of("mobile");
            assertThat(deviceType.isMobile()).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 타입 이름이면 예외가 발생한다")
        void invalidTypeNameThrowsException() {
            assertThatThrownBy(() -> DeviceType.of("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DeviceType");
        }
    }

    @Nested
    @DisplayName("of(Type) 팩토리 메서드 테스트")
    class OfTypeFactoryTest {

        @Test
        @DisplayName("Type Enum으로 생성한다")
        void createByTypeEnum() {
            DeviceType deviceType = DeviceType.of(DeviceType.Type.MOBILE);
            assertThat(deviceType.isMobile()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusCheckTest {

        @Test
        @DisplayName("MOBILE 타입은 isMobile()이 true이고 다른 것들은 false이다")
        void mobileTypeChecks() {
            DeviceType mobile = DeviceType.of(DeviceType.Type.MOBILE);
            assertThat(mobile.isMobile()).isTrue();
            assertThat(mobile.isTablet()).isFalse();
            assertThat(mobile.isDesktop()).isFalse();
        }

        @Test
        @DisplayName("TABLET 타입은 isTablet()이 true이다")
        void tabletTypeChecks() {
            DeviceType tablet = DeviceType.of(DeviceType.Type.TABLET);
            assertThat(tablet.isTablet()).isTrue();
            assertThat(tablet.isMobile()).isFalse();
            assertThat(tablet.isDesktop()).isFalse();
        }

        @Test
        @DisplayName("DESKTOP 타입은 isDesktop()이 true이다")
        void desktopTypeChecks() {
            DeviceType desktop = DeviceType.of(DeviceType.Type.DESKTOP);
            assertThat(desktop.isDesktop()).isTrue();
            assertThat(desktop.isMobile()).isFalse();
            assertThat(desktop.isTablet()).isFalse();
        }
    }

    @Nested
    @DisplayName("이름 반환 메서드 테스트")
    class NameMethodsTest {

        @Test
        @DisplayName("getTypeName()은 enum 이름을 반환한다")
        void getTypeNameReturnsEnumName() {
            DeviceType mobile = DeviceType.of(DeviceType.Type.MOBILE);
            assertThat(mobile.getTypeName()).isEqualTo("MOBILE");
        }

        @Test
        @DisplayName("getDisplayName()은 표시 이름을 반환한다")
        void getDisplayNameReturnsDisplayName() {
            DeviceType mobile = DeviceType.of(DeviceType.Type.MOBILE);
            assertThat(mobile.getDisplayName()).isEqualTo("Mobile");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 타입이면 동일하다")
        void sameTypeAreEqual() {
            DeviceType mobile1 = DeviceType.of(DeviceType.Type.MOBILE);
            DeviceType mobile2 = DeviceType.of(DeviceType.Type.MOBILE);

            assertThat(mobile1).isEqualTo(mobile2);
            assertThat(mobile1.hashCode()).isEqualTo(mobile2.hashCode());
        }

        @Test
        @DisplayName("다른 타입이면 다르다")
        void differentTypesAreNotEqual() {
            DeviceType mobile = DeviceType.of(DeviceType.Type.MOBILE);
            DeviceType desktop = DeviceType.of(DeviceType.Type.DESKTOP);

            assertThat(mobile).isNotEqualTo(desktop);
        }
    }
}
