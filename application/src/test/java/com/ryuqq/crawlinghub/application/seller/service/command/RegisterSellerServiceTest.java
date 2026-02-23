package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.component.SellerPersistenceValidator;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
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
 * <p>Mockist 스타일 테스트: Validator/Factory/CommandManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 테스트")
class RegisterSellerServiceTest {

    @Mock private SellerPersistenceValidator validator;

    @Mock private SellerCommandFactory commandFactory;

    @Mock private SellerCommandManager commandManager;

    @InjectMocks private RegisterSellerService service;

    @Nested
    @DisplayName("execute() 셀러 등록 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 중복 없는 신규 셀러 등록 시 SellerId 반환")
        void shouldRegisterNewSellerAndReturnId() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller", "seller-name");
            Seller newSeller = SellerFixture.aNewActiveSeller();

            given(commandFactory.create(command)).willReturn(newSeller);
            given(commandManager.persist(newSeller)).willReturn(SellerId.of(1L));

            // When
            long result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(1L);
            then(commandFactory).should().create(command);
            then(validator).should().validateForRegistration(newSeller);
            then(commandManager).should().persist(newSeller);
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowExceptionWhenMustItSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("duplicate-mustit", "seller-name");
            Seller newSeller = SellerFixture.aNewActiveSeller();

            given(commandFactory.create(command)).willReturn(newSeller);
            doThrow(new DuplicateMustItSellerIdException("duplicate-mustit"))
                    .when(validator)
                    .validateForRegistration(newSeller);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);

            then(commandManager).should(never()).persist(newSeller);
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowExceptionWhenSellerNameDuplicated() {
            // Given
            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller", "duplicate-name");
            Seller newSeller = SellerFixture.aNewActiveSeller();

            given(commandFactory.create(command)).willReturn(newSeller);
            doThrow(new DuplicateSellerNameException("duplicate-name"))
                    .when(validator)
                    .validateForRegistration(newSeller);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateSellerNameException.class);

            then(commandManager).should(never()).persist(newSeller);
        }
    }
}
