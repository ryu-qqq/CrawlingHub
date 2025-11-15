package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustItSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;
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
 * @since 2025-11-07
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
        @DisplayName("유효한 셀러 정보가 주어지면")
        class Context_with_valid_seller_info {

            private RegisterSellerCommand command;
            private MustItSeller savedSeller;

            @BeforeEach
            void setUp() {
                // Given: 유효한 셀러 등록 정보
                command = RegisterSellerCommandFixture.create();

                // And: 중복되지 않음
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                // And: 저장 성공
                savedSeller = MustitSellerFixture.createActive();
                given(saveSellerPort.save(any(MustItSeller.class)))
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
                then(saveSellerPort).should().save(any(MustItSeller.class));

                // And: 등록된 셀러 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.sellerCode()).isEqualTo(savedSeller.getSellerCode());
                assertThat(response.sellerName()).isEqualTo(savedSeller.getSellerNameValue());
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("특정 셀러 코드로 등록할 때")
        class Context_with_specific_seller_code {

            private RegisterSellerCommand command;
            private String customSellerCode = "CUSTOM001";

            @BeforeEach
            void setUp() {
                // Given: 특정 셀러 코드로 등록
                command = RegisterSellerCommandFixture.createWithCode(customSellerCode);

                // And: 중복되지 않음
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                // And: 저장 성공
                MustItSeller savedSeller = MustitSellerFixture.createWithCode(customSellerCode);
                given(saveSellerPort.save(any(MustItSeller.class)))
                    .willReturn(savedSeller);
            }

            @Test
            @DisplayName("지정한 셀러 코드로 등록된다")
            void it_registers_with_custom_code() {
                // When: 셀러 등록 실행
                SellerResponse response = sut.execute(command);

                // Then: 중복 체크가 수행됨
                then(loadSellerPort).should().findByCode(customSellerCode);

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustItSeller.class));

                // And: 지정한 셀러 코드로 등록됨
                assertThat(response).isNotNull();
                assertThat(response.sellerCode()).isEqualTo(customSellerCode);
            }
        }

        @Nested
        @DisplayName("특정 셀러 이름으로 등록할 때")
        class Context_with_specific_seller_name {

            private RegisterSellerCommand command;
            private String customSellerName = "커스텀셀러";

            @BeforeEach
            void setUp() {
                // Given: 특정 셀러 이름으로 등록
                command = RegisterSellerCommandFixture.createWithName(customSellerName);

                // And: 중복되지 않음
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                // And: 저장 성공
                MustItSeller savedSeller = MustitSellerFixture.createWithName(customSellerName);
                given(saveSellerPort.save(any(MustItSeller.class)))
                    .willReturn(savedSeller);
            }

            @Test
            @DisplayName("지정한 셀러 이름으로 등록된다")
            void it_registers_with_custom_name() {
                // When: 셀러 등록 실행
                SellerResponse response = sut.execute(command);

                // Then: 중복 체크가 수행됨
                then(loadSellerPort).should().findByCode(any());

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustItSeller.class));

                // And: 지정한 셀러 이름으로 등록됨
                assertThat(response).isNotNull();
                assertThat(response.sellerName()).isEqualTo(customSellerName);
            }
        }

        @Nested
        @DisplayName("이미 존재하는 셀러 코드가 주어지면")
        class Context_with_duplicate_seller_code {

            private RegisterSellerCommand command;
            private SellerQueryDto existingSeller;

            @BeforeEach
            void setUp() {
                // Given: 등록하려는 셀러 정보
                command = RegisterSellerCommandFixture.create();

                // And: 이미 동일한 셀러 코드가 존재함
                MustItSeller existing = MustitSellerFixture.createActive();
                existingSeller = new SellerQueryDto(
                    existing.getIdValue(),
                    existing.getSellerCode(),
                    existing.getSellerNameValue(),
                    existing.getStatus(),
                    existing.getTotalProductCount(),
                    existing.getLastCrawledAt(),
                    existing.getCreatedAt(),
                    existing.getUpdatedAt()
                );
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.of(existingSeller));
            }

            @Test
            @DisplayName("DuplicateSellerCodeException을 발생시킨다")
            void it_throws_duplicate_seller_code_exception() {
                // When & Then: 셀러 등록 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(DuplicateSellerCodeException.class)
                    .hasMessageContaining(command.sellerCode());

                // And: 중복 체크가 수행됨
                then(loadSellerPort).should().findByCode(command.sellerCode());

                // And: 셀러는 저장되지 않음
                then(saveSellerPort).should(never()).save(any(MustItSeller.class));
            }
        }

        @Nested
        @DisplayName("신규 셀러 등록 시")
        class Context_new_seller_registration {

            @Test
            @DisplayName("셀러는 ACTIVE 상태로 생성된다")
            void new_seller_is_created_with_active_status() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.create();
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                MustItSeller activeSeller = MustitSellerFixture.createActive();
                given(saveSellerPort.save(any(MustItSeller.class)))
                    .willReturn(activeSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
            }

            @Test
            @DisplayName("셀러는 총 상품 수 0으로 시작한다")
            void new_seller_starts_with_zero_products() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.create();
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                MustItSeller newSeller = MustitSellerFixture.createWithProductCount(0);
                given(saveSellerPort.save(any(MustItSeller.class)))
                    .willReturn(newSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.totalProductCount()).isZero();
            }
        }

        @Nested
        @DisplayName("비즈니스 규칙 검증")
        class Context_business_rules {

            @Test
            @DisplayName("중복 체크는 저장보다 먼저 수행된다")
            void duplicate_check_happens_before_save() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.create();
                MustItSeller existingSeller = MustitSellerFixture.createActive();
                SellerQueryDto queryDto = new SellerQueryDto(
                    existingSeller.getIdValue(),
                    existingSeller.getSellerCode(),
                    existingSeller.getSellerNameValue(),
                    existingSeller.getStatus(),
                    existingSeller.getTotalProductCount(),
                    existingSeller.getLastCrawledAt(),
                    existingSeller.getCreatedAt(),
                    existingSeller.getUpdatedAt()
                );
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.of(queryDto));

                // When & Then
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(DuplicateSellerCodeException.class);

                // And: save는 호출되지 않음
                then(saveSellerPort).should(never()).save(any(MustItSeller.class));
            }

            @Test
            @DisplayName("셀러 등록은 트랜잭션 내에서 수행된다")
            void seller_registration_is_transactional() {
                // Given
                RegisterSellerCommand command = RegisterSellerCommandFixture.create();
                given(loadSellerPort.findByCode(anyString()))
                    .willReturn(Optional.empty());

                MustItSeller savedSeller = MustitSellerFixture.createActive();
                given(saveSellerPort.save(any(MustItSeller.class)))
                    .willReturn(savedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then: 트랜잭션 내에서 중복 체크 → 저장 순서 보장
                then(loadSellerPort).should().findByCode(anyString());
                then(saveSellerPort).should().save(any(MustItSeller.class));
                assertThat(response).isNotNull();
            }
        }
    }
}
