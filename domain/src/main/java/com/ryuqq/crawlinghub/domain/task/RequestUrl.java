package com.ryuqq.crawlinghub.domain.crawl.task;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * 요청 URL Value Object
 */
public class RequestUrl {

    private final String url;

    private RequestUrl(String url) {
        validateUrl(url);
        this.url = url;
    }

    public static RequestUrl of(String url) {
        return new RequestUrl(url);
    }

    private static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL은 필수입니다");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("유효하지 않은 URL 형식입니다: " + url, e);
        }
    }

    public String getValue() {
        return url;
    }

    public boolean isSameAs(RequestUrl other) {
        if (other == null) {
            return false;
        }
        return this.url.equals(other.url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestUrl that = (RequestUrl) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return url;
    }
}
