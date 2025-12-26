package com.ryuqq.crawlinghub.adapter.in.rest.common.error;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class ErrorMapperRegistry {
    private final List<ErrorMapper> mappers;

    public ErrorMapperRegistry(List<ErrorMapper> mappers) {
        this.mappers = mappers;
    }

    public Optional<ErrorMapper.MappedError> map(DomainException ex, Locale locale) {
        return mappers.stream()
                .filter(m -> m.supports(ex.code()))
                .findFirst()
                .map(m -> m.map(ex, locale));
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
