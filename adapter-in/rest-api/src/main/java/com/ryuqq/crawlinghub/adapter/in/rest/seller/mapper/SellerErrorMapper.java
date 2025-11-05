package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.DomainException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerCodeException;
import com.ryuqq.crawlinghub.domain.seller.exception.InactiveSellerException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerErrorCode;

import java.net.URI;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Seller 도메인 예외를 HTTP 응답으로 변환하는 ErrorMapper
 *
 * <p>Seller Bounded Context의 모든 예외를 적절한 HTTP 상태 코드와 메시지로 매핑합니다.</p>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>SellerNotFoundException → 404 Not Found</li>
 *   <li>InactiveSellerException → 409 Conflict</li>
 *   <li>DuplicateSellerCodeException → 409 Conflict</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith("SELLER-");
    }

    @Override
    public ErrorMapper.MappedError map(DomainException ex, Locale locale) {
        if (!(ex instanceof SellerException sellerException)) {
            throw new IllegalArgumentException("SellerException이 아닌 예외입니다: " + ex.getClass());
        }

        return switch (sellerException) {
            case SellerNotFoundException notFound -> new ErrorMapper.MappedError(
                HttpStatus.NOT_FOUND,
                "Seller Not Found",
                notFound.message(),
                URI.create("/errors/seller-not-found")
            );

            case InactiveSellerException inactive -> new ErrorMapper.MappedError(
                HttpStatus.CONFLICT,
                "Seller Inactive",
                inactive.message(),
                URI.create("/errors/seller-inactive")
            );

            case DuplicateSellerCodeException duplicate -> new ErrorMapper.MappedError(
                HttpStatus.CONFLICT,
                "Duplicate Seller Code",
                duplicate.message(),
                URI.create("/errors/duplicate-seller-code")
            );
        };
    }
}

