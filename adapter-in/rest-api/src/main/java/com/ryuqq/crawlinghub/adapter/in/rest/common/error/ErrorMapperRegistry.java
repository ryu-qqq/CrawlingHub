package com.ryuqq.crawlinghub.adapter.in.rest.common.error;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorMapperRegistry {

    private static final Logger log = LoggerFactory.getLogger(ErrorMapperRegistry.class);

    private final List<ErrorMapper> mappers;

    public ErrorMapperRegistry(List<ErrorMapper> mappers) {
        this.mappers = mappers;
    }

    public Optional<ErrorMapper.MappedError> map(DomainException ex, Locale locale) {
        Optional<ErrorMapper.MappedError> result =
                mappers.stream()
                        .filter(mapper -> mapper.supports(ex))
                        .findFirst()
                        .map(mapper -> mapper.map(ex, locale));

        if (result.isEmpty()) {
            log.warn("No ErrorMapper found for DomainException code={}", ex.code());
        }

        return result;
    }

    public ErrorMapper.MappedError defaultMapping(DomainException ex) {
        HttpStatus status = HttpStatus.resolve(ex.httpStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        String title = status.is4xxClientError() ? "Client Error" : "Server Error";
        if (status == HttpStatus.NOT_FOUND) {
            title = "Not Found";
        } else if (status == HttpStatus.BAD_REQUEST) {
            title = "Bad Request";
        } else if (status == HttpStatus.CONFLICT) {
            title = "Conflict";
        }
        return new ErrorMapper.MappedError(
                status,
                title,
                ex.getMessage() != null ? ex.getMessage() : "Invalid request",
                URI.create("about:blank"));
    }
}
