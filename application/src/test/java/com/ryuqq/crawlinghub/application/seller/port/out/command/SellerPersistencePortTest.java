package com.ryuqq.crawlinghub.application.seller.port.out.command;

import java.lang.reflect.Method;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerPersistencePort 명세")
class SellerPersistencePortTest {

    @Test
    @DisplayName("SellerPersistencePort는 인터페이스여야 한다")
    void shouldBeInterface() {
        assertThat(SellerPersistencePort.class.isInterface())
            .as("SellerPersistencePort must be declared as interface")
            .isTrue();
    }

    @Nested
    @DisplayName("필수 메서드 시그니처")
    class MethodSignatureTest {

        @Test
        @DisplayName("persist 메서드는 Seller를 저장하고 SellerId를 반환해야 한다")
        void shouldDeclarePersistMethod() throws NoSuchMethodException {
            Method method = SellerPersistencePort.class.getMethod("persist", Seller.class);

            assertThat(method.getReturnType())
                .as("persist must return the SellerId value object")
                .isEqualTo(SellerId.class);
        }
    }
}

