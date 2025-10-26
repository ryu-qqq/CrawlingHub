package com.ryuqq.crawlinghub.application.mustit.seller.usecase;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.DuplicateSellerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RegisterMustitSellerUseCase 단위 테스트
 * <p>
 * Port는 Mocking하여 UseCase의 비즈니스 로직만 검증합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterMustitSellerUseCase 단위 테스트")
class RegisterMustitSellerServiceTest {

    @Mock
    private LoadMustitSellerPort loadMustitSellerPort;

    @Mock
    private SaveMustitSellerPort saveMustitSellerPort;

    private RegisterMustitSellerService useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterMustitSellerService(loadMustitSellerPort, saveMustitSellerPort);
    }

    @Test
    @DisplayName("유효한 Command로 셀러를 등록할 수 있다")
    void registerSellerWithValidCommand() {
        // given
        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );

        when(loadMustitSellerPort.existsBySellerId(command.sellerId())).thenReturn(false);
        when(saveMustitSellerPort.save(any(MustitSeller.class))).thenAnswer(invocation -> {
            MustitSeller seller = invocation.getArgument(0);
            return seller;
        });

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo("SELLER001");

        // Port 호출 검증
        verify(loadMustitSellerPort, times(1)).existsBySellerId("SELLER001");
        verify(saveMustitSellerPort, times(1)).save(any(MustitSeller.class));

        // 저장된 셀러 검증
        ArgumentCaptor<MustitSeller> sellerCaptor = ArgumentCaptor.forClass(MustitSeller.class);
        verify(saveMustitSellerPort).save(sellerCaptor.capture());
        MustitSeller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller.getSellerId()).isEqualTo("SELLER001");
        assertThat(savedSeller.getName()).isEqualTo("Test Seller");
        assertThat(savedSeller.isActive()).isTrue(); // 기본값: 활성
        assertThat(savedSeller.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.DAILY);
        assertThat(savedSeller.getCrawlInterval().getIntervalValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("중복된 sellerId로 등록 시도 시 예외가 발생한다")
    void throwExceptionWhenSellerIdAlreadyExists() {
        // given
        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );

        when(loadMustitSellerPort.existsBySellerId(command.sellerId())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DuplicateSellerException.class)
                .hasMessageContaining("SELLER001");

        // 저장 Port는 호출되지 않아야 함
        verify(saveMustitSellerPort, never()).save(any(MustitSeller.class));
    }

    @Test
    @DisplayName("Command가 null이면 예외가 발생한다")
    void throwExceptionWhenCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("command must not be null");
    }

    @Test
    @DisplayName("다양한 크롤링 주기로 셀러를 등록할 수 있다")
    void registerSellerWithDifferentCrawlIntervals() {
        // given - HOURLY
        RegisterMustitSellerCommand hourlyCommand = new RegisterMustitSellerCommand(
                "SELLER_HOURLY",
                "Hourly Seller",
                CrawlIntervalType.HOURLY,
                6
        );

        when(loadMustitSellerPort.existsBySellerId("SELLER_HOURLY")).thenReturn(false);
        when(saveMustitSellerPort.save(any(MustitSeller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(hourlyCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo("SELLER_HOURLY");

        ArgumentCaptor<MustitSeller> sellerCaptor = ArgumentCaptor.forClass(MustitSeller.class);
        verify(saveMustitSellerPort).save(sellerCaptor.capture());
        MustitSeller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.HOURLY);
        assertThat(savedSeller.getCrawlInterval().getIntervalValue()).isEqualTo(6);
        assertThat(savedSeller.getCrawlInterval().getCronExpression()).isEqualTo("0 0/6 * * ? *");
    }

    @Test
    @DisplayName("생성된 셀러의 크롤링 주기는 올바른 cron 표현식을 포함한다")
    void createdSellerHasCorrectCronExpression() {
        // given
        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                "SELLER_WEEKLY",
                "Weekly Seller",
                CrawlIntervalType.WEEKLY,
                2
        );

        when(loadMustitSellerPort.existsBySellerId("SELLER_WEEKLY")).thenReturn(false);
        when(saveMustitSellerPort.save(any(MustitSeller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        useCase.execute(command);

        // then
        ArgumentCaptor<MustitSeller> sellerCaptor = ArgumentCaptor.forClass(MustitSeller.class);
        verify(saveMustitSellerPort).save(sellerCaptor.capture());
        MustitSeller savedSeller = sellerCaptor.getValue();

        // AWS EventBridge용 cron 표현식 검증
        assertThat(savedSeller.getCrawlInterval().getCronExpression()).isEqualTo("0 0 0 ? * 1/2 *");
    }

    @Test
    @DisplayName("UseCase 생성 시 null Port가 주입되면 예외가 발생한다")
    void throwExceptionWhenPortIsNull() {
        // when & then - loadMustitSellerPort가 null
        assertThatThrownBy(() -> new RegisterMustitSellerService(null, saveMustitSellerPort))
                .isInstanceOf(NullPointerException.class);

        // when & then - saveMustitSellerPort가 null
        assertThatThrownBy(() -> new RegisterMustitSellerService(loadMustitSellerPort, null))
                .isInstanceOf(NullPointerException.class);
    }
}
