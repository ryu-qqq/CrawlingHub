package com.ryuqq.crawlinghub.domain.task.input;

import java.util.Objects;

/**
 * MINI_SHOP Task Input Parameter (타입 안전)
 *
 * <p>미니샵 실제 크롤링용 파라미터 (pageSize=500)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class MiniShopTaskInputParam extends TaskInputParam {

    private final Long sellerId;
    private final int pageNo;
    private final int pageSize;
    private final String order;

    private MiniShopTaskInputParam(Long sellerId, int pageNo, int pageSize, String order) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("sellerId는 필수이며 양수여야 합니다");
        }
        if (pageNo < 0) {
            throw new IllegalArgumentException("pageNo는 0 이상이어야 합니다");
        }
        if (pageSize != 500) {
            throw new IllegalArgumentException("MINI_SHOP Task는 pageSize=500이어야 합니다");
        }

        this.sellerId = sellerId;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.order = order;
    }

    public static MiniShopTaskInputParam of(Long sellerId, int pageNo) {
        return new MiniShopTaskInputParam(sellerId, pageNo, 500, "LATEST");
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
        if (!(o instanceof MiniShopTaskInputParam that)) return false;
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
        return "MiniShopTaskInputParam{" +
               "sellerId=" + sellerId +
               ", pageNo=" + pageNo +
               ", pageSize=" + pageSize +
               ", order='" + order + '\'' +
               '}';
    }
}
