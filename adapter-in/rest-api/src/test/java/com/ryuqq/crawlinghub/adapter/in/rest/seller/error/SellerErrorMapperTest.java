package com.ryuqq.crawlinghub.adapter.in.rest.seller.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import java.net.URI;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;

/**
 * SellerErrorMapper 단위 테스트
 *
 * <p>Seller 도메인 예외 → HTTP 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>PREFIX 기반 supports() 테스트
 *   <li>각 Seller 예외별 HttpStatus 매핑 검증
 *   <li>I18N 메시지 변환 검증 (한글/영문)
 *   <li>RFC 7807 Type URI 생성 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerErrorMapper 단위 테스트")
class SellerErrorMapperTest {

    private SellerErrorMapper sellerErrorMapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        // MessageSource 설정 (messages_ko.properties, messages_en.properties 로드)
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        this.messageSource = messageSource;

        sellerErrorMapper = new SellerErrorMapper(messageSource);
    }

    // ===============================================
    // supports() 테스트
    // ===============================================

    @Test
    @DisplayName("SELLER- prefix를 가진 에러 코드는 supports()가 true를 반환한다")
    void supports_WhenSellerPrefix_ShouldReturnTrue() {
        // given
        DomainException ex = createDomainException("SELLER-001");

        // when
        boolean result = sellerErrorMapper.supports(ex);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SELLER- prefix가 없는 에러 코드는 supports()가 false를 반환한다")
    void supports_WhenNonSellerPrefix_ShouldReturnFalse() {
        // given
        DomainException ex = createDomainException("ORDER-001");

        // when
        boolean result = sellerErrorMapper.supports(ex);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 에러 코드는 supports()가 false를 반환한다")
    void supports_WhenNullCode_ShouldReturnFalse() {
        // when
        boolean result = sellerErrorMapper.supports(null);

        // then
        assertThat(result).isFalse();
    }

    // ===============================================
    // map() 테스트 - SellerNotFoundException (404)
    // ===============================================

    @Test
    @DisplayName("SellerNotFoundException은 404 NOT_FOUND로 매핑된다")
    void map_SellerNotFoundException_ShouldReturn404() {
        // given
        long sellerId = 999L;
        var exception = new SellerNotFoundException(sellerId);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.type())
                .isEqualTo(URI.create("https://api.example.com/problems/seller/seller-004"));
        assertThat(result.detail()).contains("존재하지 않는 셀러입니다");
        assertThat(result.detail()).contains("999");
    }

    @Test
    @DisplayName("SellerNotFoundException은 sellerId 파라미터가 포함된 메시지로 변환된다")
    void map_SellerNotFoundException_ShouldIncludeSellerId() {
        // given
        long sellerId = 123L;
        var exception = new SellerNotFoundException(sellerId);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).isNotNull();
        assertThat(result.title()).contains("123");
    }

    @Test
    @DisplayName("SellerNotFoundException은 영문 메시지로 변환된다")
    void map_SellerNotFoundException_ShouldReturnEnglishMessage() {
        // given
        long sellerId = 123L;
        var exception = new SellerNotFoundException(sellerId);
        Locale locale = Locale.US;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).contains("Seller not found");
        assertThat(result.title()).contains("123");
    }

    // ===============================================
    // map() 테스트 - DuplicateMustItSellerIdException (409)
    // ===============================================

    @Test
    @DisplayName("DuplicateMustItSellerIdException은 409 CONFLICT로 매핑된다")
    void map_DuplicateMustItSellerIdException_ShouldReturn409() {
        // given
        String mustItSellerName = "무신사";
        var exception = new DuplicateMustItSellerIdException(mustItSellerName);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.type())
                .isEqualTo(URI.create("https://api.example.com/problems/seller/seller-001"));
        assertThat(result.detail()).contains("머스트잇 셀러");
        assertThat(result.detail()).contains("무신사");
    }

    @Test
    @DisplayName("DuplicateMustItSellerIdException은 mustItSellerName 파라미터가 포함된 메시지로 변환된다")
    void map_DuplicateMustItSellerIdException_ShouldIncludeMustItSellerName() {
        // given
        String mustItSellerName = "무신사";
        var exception = new DuplicateMustItSellerIdException(mustItSellerName);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).isNotNull();
        assertThat(result.title()).contains("무신사");
    }

    // ===============================================
    // map() 테스트 - DuplicateSellerNameException (409)
    // ===============================================

    @Test
    @DisplayName("DuplicateSellerNameException은 409 CONFLICT로 매핑된다")
    void map_DuplicateSellerNameException_ShouldReturn409() {
        // given
        String sellerName = "스마트스토어";
        var exception = new DuplicateSellerNameException(sellerName);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.type())
                .isEqualTo(URI.create("https://api.example.com/problems/seller/seller-002"));
        assertThat(result.detail()).contains("셀러 이름");
        assertThat(result.detail()).contains("스마트스토어");
    }

    @Test
    @DisplayName("DuplicateSellerNameException은 영문 메시지로 변환된다")
    void map_DuplicateSellerNameException_ShouldReturnEnglishMessage() {
        // given
        String sellerName = "SmartStore";
        var exception = new DuplicateSellerNameException(sellerName);
        Locale locale = Locale.US;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).contains("Seller name already exists");
        assertThat(result.title()).contains("SmartStore");
    }

    // ===============================================
    // map() 테스트 - SellerHasActiveSchedulersException (400)
    // ===============================================

    @Test
    @DisplayName("SellerHasActiveSchedulersException은 400 BAD_REQUEST로 매핑된다")
    void map_SellerHasActiveSchedulersException_ShouldReturn400() {
        // given
        long sellerId = 123L;
        int activeSchedulerCount = 5;
        var exception = new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.type())
                .isEqualTo(URI.create("https://api.example.com/problems/seller/seller-003"));
        assertThat(result.detail()).contains("스케줄러");
        assertThat(result.detail()).contains("123");
        assertThat(result.detail()).contains("5");
    }

    @Test
    @DisplayName(
            "SellerHasActiveSchedulersException은 sellerId와 activeSchedulerCount 파라미터가 포함된 메시지로"
                    + " 변환된다")
    void map_SellerHasActiveSchedulersException_ShouldIncludeParameters() {
        // given
        long sellerId = 123L;
        int activeSchedulerCount = 5;
        var exception = new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);
        Locale locale = Locale.KOREA;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).isNotNull();
        assertThat(result.title()).contains("123");
        assertThat(result.title()).contains("5");
    }

    @Test
    @DisplayName("SellerHasActiveSchedulersException은 영문 메시지로 변환된다")
    void map_SellerHasActiveSchedulersException_ShouldReturnEnglishMessage() {
        // given
        long sellerId = 123L;
        int activeSchedulerCount = 5;
        var exception = new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);
        Locale locale = Locale.US;

        // when
        MappedError result = sellerErrorMapper.map(exception, locale);

        // then
        assertThat(result.title()).contains("Cannot deactivate seller");
        assertThat(result.title()).contains("123");
        assertThat(result.title()).contains("5");
    }

    private DomainException createDomainException(String code) {
        ErrorCode errorCode = Mockito.mock(ErrorCode.class);
        given(errorCode.getCode()).willReturn(code);
        return new DomainException(errorCode, "test") {
            @Override
            public String code() {
                return code;
            }
        };
    }
}
