package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * RegisterSellerService 단위 테스트
 *
 * <p>셀러 등록 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 단위 테스트")
class RegisterSellerServiceTest {

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private SaveSellerPort saveSellerPort;

    @InjectMocks
    private RegisterSellerService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("유효한 신규 셀러 정보가 주어지면")
        class Context_with_valid_new_seller {

            private RegisterSellerCommand command;
            private MustitSeller savedSeller;

            @BeforeEach
            void setUp() {
                // Given: 유효한 신규 셀러 정보
                command = RegisterSellerCommandFixture.create();
                savedSeller = MustitSellerFixture.createWithId(1L);

                // And: 중복된 셀러 코드가 없음
                given(loadSellerPort.findByCode(command.sellerCode()))
                    .willReturn(Optional.empty());

                // And: 셀러 저장 성공
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(savedSeller);
            }

            @Test
            @DisplayName("셀러를 등록하고 응답을 반환한다")
            void it_registers_seller_and_returns_response() {
                // When: 셀러 등록 실행
                SellerResponse response = sut.execute(command);

                // Then: 중복 체크가 수행됨
                then(loadSellerPort).should().findByCode(command.sellerCode());

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustitSeller.class));

                // And: 저장된 셀러 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.sellerId()).isEqualTo(savedSeller.getIdValue());
                assertThat(response.sellerCode()).isEqualTo(savedSeller.getSellerCode());
                assertThat(response.sellerName()).isEqualTo(savedSeller.getSellerName());
                assertThat(response.status()).isEqualTo(savedSeller.getStatus());
            }
        }

        @Nested
        @DisplayName("이미 존재하는 셀러 코드가 주어지면")
        class Context_with_duplicate_seller_code {

            private RegisterSellerCommand command;
            private MustitSeller existingSeller;

            @BeforeEach
            void setUp() {
                // Given: 등록하려는 셀러 정보
                command = RegisterSellerCommandFixture.create();

                // And: 이미 존재하는 셀러
                existingSeller = MustitSellerFixture.createWithCode(command.sellerCode());
                given(loadSellerPort.findByCode(command.sellerCode()))
                    .willReturn(Optional.of(existingSeller));
            }

            @Test
            @DisplayName("DuplicateSellerCodeException을 발생시킨다")
            void it_throws_duplicate_seller_code_exception() {
                // When & Then: 셀러 등록 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(DuplicateSellerCodeException.class)
                    .hasMessageContaining(command.sellerCode());

                // And: 중복 체크는 수행됨
                then(loadSellerPort).should().findByCode(command.sellerCode());

                // And: 셀러는 저장되지 않음
                then(saveSellerPort).should(never()).save(any(MustitSeller.class));
            }
        }

        @Nested
        @DisplayName("다양한 셀러 코드로 등록 시")
        class Context_with_various_seller_codes {

            @Test
            @DisplayName("영문 대문자 코드는 성공한다")
            void it_accepts_uppercase_code() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithCode("ABC123");
                MustitSeller savedSeller = MustitSellerFixture.createWithCode("ABC123");

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerCode()).isEqualTo("ABC123");
            }

            @Test
            @DisplayName("숫자만 포함된 코드는 성공한다")
            void it_accepts_numeric_code() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithCode("12345");
                MustitSeller savedSeller = MustitSellerFixture.createWithCode("12345");

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerCode()).isEqualTo("12345");
            }

            @Test
            @DisplayName("특수문자를 포함한 코드는 성공한다")
            void it_accepts_code_with_special_chars() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithCode("SEL-001");
                MustitSeller savedSeller = MustitSellerFixture.createWithCode("SEL-001");

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerCode()).isEqualTo("SEL-001");
            }
        }

        @Nested
        @DisplayName("다양한 셀러 이름으로 등록 시")
        class Context_with_various_seller_names {

            @Test
            @DisplayName("한글 이름은 성공한다")
            void it_accepts_korean_name() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithName("테스트셀러");
                MustitSeller savedSeller = MustitSellerFixture.createWithName("테스트셀러");

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerName()).isEqualTo("테스트셀러");
            }

            @Test
            @DisplayName("영문 이름은 성공한다")
            void it_accepts_english_name() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithName("Test Seller");
                MustitSeller savedSeller = MustitSellerFixture.createWithName("Test Seller");

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerName()).isEqualTo("Test Seller");
            }

            @Test
            @DisplayName("긴 이름은 성공한다")
            void it_accepts_long_name() {
                // Given
                String longName = "매우 긴 셀러 이름입니다 아주 길어요 정말 길어요";
                RegisterSellerCommand command = RegisterSellerCommandFixture.createWithName(longName);
                MustitSeller savedSeller = MustitSellerFixture.createWithName(longName);

                given(loadSellerPort.findByCode(anyString())).willReturn(Optional.empty());
                given(saveSellerPort.save(any(MustitSeller.class))).willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.sellerName()).isEqualTo(longName);
            }
        }
    }
}
