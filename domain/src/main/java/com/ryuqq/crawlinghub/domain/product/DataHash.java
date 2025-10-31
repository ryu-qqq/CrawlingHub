package com.ryuqq.crawlinghub.domain.product;

import java.util.Objects;

/**
 * 데이터 해시 Value Object (SHA-256)
 */
public class DataHash {

    private final String hash;

    private DataHash(String hash) {
        validateHash(hash);
        this.hash = hash;
    }

    public static DataHash of(String hash) {
        return new DataHash(hash);
    }

    private static void validateHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("해시값은 필수입니다");
        }
        if (hash.length() != 64) {
            throw new IllegalArgumentException("SHA-256 해시는 64자여야 합니다");
        }
    }

    public String getValue() {
        return hash;
    }

    public boolean isSameAs(DataHash other) {
        if (other == null) {
            return false;
        }
        return this.hash.equals(other.hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataHash that = (DataHash) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return hash;
    }
}
