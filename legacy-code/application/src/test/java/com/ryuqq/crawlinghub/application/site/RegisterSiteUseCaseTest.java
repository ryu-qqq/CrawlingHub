package com.ryuqq.crawlinghub.application.site;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.application.site.usecase.DuplicateSiteException;
import com.ryuqq.crawlinghub.application.site.usecase.RegisterSiteCommand;
import com.ryuqq.crawlinghub.application.site.usecase.RegisterSiteUseCase;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterSiteUseCaseTest {

    @Mock
    private SaveSitePort saveSitePort;

    @Mock
    private LoadSitePort loadSitePort;

    @InjectMocks
    private RegisterSiteUseCase registerSiteUseCase;

    @Test
    @DisplayName("사이트 등록 UseCase 정상 실행")
    void shouldRegisterSiteSuccessfully() {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                "Test Site",
                "https://api.test.com",
                "REST_API"
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(false);

        CrawlSite savedSite = CrawlSite.reconstitute(
                SiteId.of(1L),
                command.siteName(),
                command.baseUrl(),
                SiteType.REST_API,
                true
        );
        when(saveSitePort.save(any(CrawlSite.class))).thenReturn(savedSite);

        // when
        CrawlSite result = registerSiteUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSiteId()).isEqualTo(SiteId.of(1L));
        assertThat(result.getSiteName()).isEqualTo("Test Site");
        assertThat(result.getBaseUrl()).isEqualTo("https://api.test.com");
        assertThat(result.getSiteType()).isEqualTo(SiteType.REST_API);

        verify(loadSitePort, times(1)).existsBySiteName(command.siteName());
        verify(saveSitePort, times(1)).save(any(CrawlSite.class));
    }

    @Test
    @DisplayName("중복 사이트 이름으로 등록 시 예외 발생")
    void shouldThrowExceptionWhenSiteNameAlreadyExists() {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                "Duplicate Site",
                "https://api.duplicate.com",
                "REST_API"
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registerSiteUseCase.execute(command))
                .isInstanceOf(DuplicateSiteException.class)
                .hasMessageContaining("Site name already exists");

        verify(loadSitePort, times(1)).existsBySiteName(command.siteName());
        verify(saveSitePort, never()).save(any(CrawlSite.class));
    }

    @Test
    @DisplayName("잘못된 SiteType으로 등록 시 예외 발생")
    void shouldThrowExceptionWhenSiteTypeIsInvalid() {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                "Test Site",
                "https://api.test.com",
                "INVALID_TYPE"
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> registerSiteUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid site type");

        verify(loadSitePort, times(1)).existsBySiteName(command.siteName());
        verify(saveSitePort, never()).save(any(CrawlSite.class));
    }

    @Test
    @DisplayName("대소문자 구분 없이 SiteType 처리")
    void shouldHandleSiteTypeCaseInsensitively() {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                "Test Site",
                "https://api.test.com",
                "rest_api"
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(false);

        CrawlSite savedSite = CrawlSite.reconstitute(
                SiteId.of(1L),
                command.siteName(),
                command.baseUrl(),
                SiteType.REST_API,
                true
        );
        when(saveSitePort.save(any(CrawlSite.class))).thenReturn(savedSite);

        // when
        CrawlSite result = registerSiteUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSiteType()).isEqualTo(SiteType.REST_API);
    }

    @Test
    @DisplayName("동시성 문제로 DataIntegrityViolationException 발생 시 처리")
    void shouldHandleRaceConditionWithDataIntegrityViolation() {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                "Race Condition Site",
                "https://api.race.com",
                "REST_API"
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(false);
        when(saveSitePort.save(any(CrawlSite.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // when & then
        assertThatThrownBy(() -> registerSiteUseCase.execute(command))
                .isInstanceOf(DuplicateSiteException.class)
                .hasMessageContaining("Site name already exists");

        verify(loadSitePort, times(1)).existsBySiteName(command.siteName());
        verify(saveSitePort, times(1)).save(any(CrawlSite.class));
    }

    @ParameterizedTest
    @EnumSource(SiteType.class)
    @DisplayName("다양한 SiteType으로 사이트 등록 가능")
    void shouldRegisterSitesWithDifferentTypes(SiteType siteType) {
        // given
        RegisterSiteCommand command = new RegisterSiteCommand(
                siteType.name() + " Site",
                "https://api." + siteType.name().toLowerCase() + ".com",
                siteType.name()
        );

        when(loadSitePort.existsBySiteName(command.siteName())).thenReturn(false);

        CrawlSite savedSite = CrawlSite.reconstitute(
                SiteId.of(1L),
                command.siteName(),
                command.baseUrl(),
                siteType,
                true
        );
        when(saveSitePort.save(any(CrawlSite.class))).thenReturn(savedSite);

        // when
        CrawlSite result = registerSiteUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSiteType()).isEqualTo(siteType);
        verify(loadSitePort, times(1)).existsBySiteName(command.siteName());
        verify(saveSitePort, times(1)).save(any(CrawlSite.class));
    }

}
