package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RegisterSellerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 테스트")
class RegisterSellerServiceTest {

    @Mock private SellerTransactionManager transactionManager;

    @Mock private SellerQueryPort sellerQueryPort;

    @Mock private SellerAssembler assembler;

    @InjectMocks private RegisterSellerService service;

    @Nested
    @DisplayName("execute() 셀러 등록 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 중복 없는 신규 셀러 등록")
        void shouldRegisterNewSellerWhenNoDuplicate() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller", "seller-name");
            Seller newSeller = SellerFixture.aNewActiveSeller();
            SellerResponse expectedResponse =
                    new SellerResponse(
                            1L,
                            "mustit-seller",
                            "seller-name",
                            true,
                            LocalDateTime.now(),
                            LocalDateTime.now());

            given(sellerQueryPort.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerQueryPort.existsBySellerName(any(SellerName.class))).willReturn(false);
            given(assembler.toDomain(command)).willReturn(newSeller);
            given(transactionManager.persist(newSeller)).willReturn(SellerId.of(1L));
            given(assembler.toResponse(newSeller)).willReturn(expectedResponse);

            // When
            SellerResponse result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(sellerQueryPort).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerQueryPort).should().existsBySellerName(any(SellerName.class));
            then(assembler).should().toDomain(command);
            then(transactionManager).should().persist(newSeller);
            then(assembler).should().toResponse(newSeller);
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowExceptionWhenMustItSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("duplicate-mustit", "seller-name");

            given(sellerQueryPort.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);

            then(sellerQueryPort).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerQueryPort).should(never()).existsBySellerName(any(SellerName.class));
            then(transactionManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowExceptionWhenSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller", "duplicate-name");

            given(sellerQueryPort.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerQueryPort.existsBySellerName(any(SellerName.class))).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateSellerNameException.class);

            then(sellerQueryPort).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerQueryPort).should().existsBySellerName(any(SellerName.class));
            then(transactionManager).should(never()).persist(any());
        }
    }
}
