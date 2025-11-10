package com.ryuqq.crawlinghub.domain.task.input;

import java.util.Objects;

/**
 * META Task Input Parameter (타입 안전)
 *
 * <p>미니샵 총 상품 수 파악용 파라미터 (pageSize=1)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class MetaTaskInputParam extends TaskInputParam {

    private final Long sellerId;
    private final int pageNo;
    private final int pageSize;
    private final String order;

    private MetaTaskInputParam(Long sellerId, int pageNo, int pageSize, String order) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("sellerId는 필수이며 양수여야 합니다");
        }
        if (pageNo < 0) {
            throw new IllegalArgumentException("pageNo는 0 이상이어야 합니다");
        }
        if (pageSize != 1) {
            throw new IllegalArgumentException("META Task는 pageSize=1이어야 합니다");
        }

        this.sellerId = sellerId;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.order = order;
    }

    public static MetaTaskInputParam of(Long sellerId) {
        return new MetaTaskInputParam(sellerId, 0, 1, "LATEST");
    }

    public Long getSellerId() {
        return sellerId;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaTaskInputParam that)) return false;
        return pageNo == that.pageNo &&
               pageSize == that.pageSize &&
               Objects.equals(sellerId, that.sellerId) &&
               Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellerId, pageNo, pageSize, order);
    }

    @Override
    public String toString() {
        return "MetaTaskInputParam{" +
               "sellerId=" + sellerId +
               ", pageNo=" + pageNo +
               ", pageSize=" + pageSize +
               ", order='" + order + '\'' +
               '}';
    }
}
