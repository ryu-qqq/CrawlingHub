package com.ryuqq.crawlinghub.adapter.in.rest.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

@Tag("unit")
@Tag("rest-api")
@Tag("error")
@DisplayName("ErrorMapperRegistry 단위 테스트")
class ErrorMapperRegistryTest {

    @Nested
    @DisplayName("map() 메서드는")
    class MapMethod {

        @Test
        @DisplayName("지원하는 mapper가 있으면 매핑 결과를 반환한다")
        void shouldReturnMappedErrorWhenMapperSupports() {
            // Given
            ErrorMapper mapper1 = Mockito.mock(ErrorMapper.class);
            ErrorMapper mapper2 = Mockito.mock(ErrorMapper.class);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            DomainException exception = createDomainException("TEST_ERROR", "Test error message");
            Locale locale = Locale.KOREAN;
            MappedError expectedError =
                    new MappedError(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            "Resource not found",
                            URI.create("https://error.example.com/TEST_ERROR"));

            given(mapper1.supports(exception)).willReturn(true);
            given(mapper1.map(exception, locale)).willReturn(expectedError);

            // When
            Optional<MappedError> result = registry.map(exception, locale);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedError);
            verify(mapper1).map(exception, locale);
        }

        @Test
        @DisplayName("첫 번째로 지원하는 mapper를 사용한다")
        void shouldUseFirstSupportingMapper() {
            // Given
            ErrorMapper mapper1 = Mockito.mock(ErrorMapper.class);
            ErrorMapper mapper2 = Mockito.mock(ErrorMapper.class);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            DomainException exception = createDomainException("TEST_ERROR", "Test error message");
            Locale locale = Locale.KOREAN;
            MappedError firstMapperError =
                    new MappedError(
                            HttpStatus.NOT_FOUND,
                            "First Mapper",
                            "First mapper detail",
                            URI.create("about:blank"));

            given(mapper1.supports(exception)).willReturn(true);
            given(mapper1.map(exception, locale)).willReturn(firstMapperError);

            // When
            Optional<MappedError> result = registry.map(exception, locale);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(firstMapperError);
            verify(mapper2, never()).map(exception, locale);
        }

        @Test
        @DisplayName("지원하는 mapper가 없으면 empty를 반환한다")
        void shouldReturnEmptyWhenNoMapperSupports() {
            // Given
            ErrorMapper mapper1 = Mockito.mock(ErrorMapper.class);
            ErrorMapper mapper2 = Mockito.mock(ErrorMapper.class);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            DomainException exception = createDomainException("UNKNOWN_ERROR", "Unknown error");
            Locale locale = Locale.KOREAN;

            given(mapper1.supports(exception)).willReturn(false);
            given(mapper2.supports(exception)).willReturn(false);

            // When
            Optional<MappedError> result = registry.map(exception, locale);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("두 번째 mapper가 지원하면 두 번째 mapper를 사용한다")
        void shouldUseSecondMapperWhenFirstDoesNotSupport() {
            // Given
            ErrorMapper mapper1 = Mockito.mock(ErrorMapper.class);
            ErrorMapper mapper2 = Mockito.mock(ErrorMapper.class);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            DomainException exception = createDomainException("TEST_ERROR", "Test error message");
            Locale locale = Locale.KOREAN;
            MappedError expectedError =
                    new MappedError(
                            HttpStatus.CONFLICT,
                            "Conflict",
                            "Resource conflict",
                            URI.create("about:blank"));

            given(mapper1.supports(exception)).willReturn(false);
            given(mapper2.supports(exception)).willReturn(true);
            given(mapper2.map(exception, locale)).willReturn(expectedError);

            // When
            Optional<MappedError> result = registry.map(exception, locale);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedError);
            verify(mapper1, never()).map(exception, locale);
            verify(mapper2).map(exception, locale);
        }
    }

    @Nested
    @DisplayName("defaultMapping() 메서드는")
    class DefaultMappingMethod {

        @Test
        @DisplayName("BAD_REQUEST 상태의 기본 매핑을 반환한다")
        void shouldReturnBadRequestDefault() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());
            DomainException exception = createDomainException("ANY_ERROR", "Any error message");

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.title()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("예외 메시지를 detail에 포함한다")
        void shouldIncludeExceptionMessageAsDetail() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());
            String errorMessage = "Specific error message";
            DomainException exception = createDomainException("ANY_ERROR", errorMessage);

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.detail()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("예외 메시지가 null이면 기본 메시지를 사용한다")
        void shouldUseDefaultMessageWhenExceptionMessageIsNull() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());
            DomainException exception = createDomainException("ANY_ERROR", null);

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.detail()).isEqualTo("Invalid request");
        }

        @Test
        @DisplayName("about:blank URI를 반환한다")
        void shouldReturnAboutBlankUri() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());
            DomainException exception = createDomainException("ANY_ERROR", "Error");

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.type()).isEqualTo(URI.create("about:blank"));
        }
    }

    @Nested
    @DisplayName("빈 mapper 목록")
    class EmptyMapperList {

        @Test
        @DisplayName("mapper가 없으면 항상 empty를 반환한다")
        void shouldReturnEmptyWhenNoMappers() {
            // Given
            ErrorMapperRegistry emptyRegistry = new ErrorMapperRegistry(List.of());
            DomainException exception = createDomainException("ANY_ERROR", "Error");

            // When
            Optional<MappedError> result = emptyRegistry.map(exception, Locale.KOREAN);

            // Then
            assertThat(result).isEmpty();
        }
    }

    private DomainException createDomainException(String code, String message) {
        ErrorCode errorCode = Mockito.mock(ErrorCode.class);
        given(errorCode.getCode()).willReturn(code);

        return new DomainException(errorCode, message) {
            @Override
            public String code() {
                return code;
            }
        };
    }
}
