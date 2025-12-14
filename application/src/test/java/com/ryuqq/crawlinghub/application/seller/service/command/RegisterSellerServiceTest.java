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
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import java.time.Instant;
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
 * <p>Mockist 스타일 테스트: Manager/Factory 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 테스트")
class RegisterSellerServiceTest {

    @Mock private SellerTransactionManager transactionManager;

    @Mock private SellerReadManager sellerReadManager;

    @Mock private SellerCommandFactory commandFactory;

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
            Instant now = Instant.now();
            SellerResponse expectedResponse =
                    new SellerResponse(1L, "mustit-seller", "seller-name", true, now, now);

            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerReadManager.existsBySellerName(any(SellerName.class))).willReturn(false);
            given(commandFactory.create(command)).willReturn(newSeller);
            given(transactionManager.persist(newSeller)).willReturn(SellerId.of(1L));
            given(assembler.toResponse(newSeller)).willReturn(expectedResponse);

            // When
            SellerResponse result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(sellerReadManager).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerReadManager).should().existsBySellerName(any(SellerName.class));
            then(commandFactory).should().create(command);
            then(transactionManager).should().persist(newSeller);
            then(assembler).should().toResponse(newSeller);
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowExceptionWhenMustItSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("duplicate-mustit", "seller-name");

            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);

            then(sellerReadManager).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerReadManager).should(never()).existsBySellerName(any(SellerName.class));
            then(transactionManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowExceptionWhenSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller", "duplicate-name");

            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerReadManager.existsBySellerName(any(SellerName.class))).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateSellerNameException.class);

            then(sellerReadManager).should().existsByMustItSellerName(any(MustItSellerName.class));
            then(sellerReadManager).should().existsBySellerName(any(SellerName.class));
            then(transactionManager).should(never()).persist(any());
        }
    }
}
