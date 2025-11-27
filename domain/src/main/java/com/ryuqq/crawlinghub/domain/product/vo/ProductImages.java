package com.ryuqq.crawlinghub.domain.product.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 상품 이미지 일급 컬렉션
 *
 * <p>상품의 모든 이미지를 관리하고 타입별 필터링, 변경 감지 등의 기능을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProductImages {

    private final List<ProductImage> images;

    private ProductImages(List<ProductImage> images) {
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
    }

    /** 빈 컬렉션 생성 */
    public static ProductImages empty() {
        return new ProductImages(Collections.emptyList());
    }

    /** 이미지 목록으로 생성 */
    public static ProductImages of(List<ProductImage> images) {
        return new ProductImages(images);
    }

    /** 썸네일 URL 목록으로 생성 */
    public static ProductImages fromThumbnailUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return empty();
        }
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            images.add(ProductImage.thumbnail(urls.get(i), i));
        }
        return new ProductImages(images);
    }

    /** URL 목록으로 생성 (fromThumbnailUrls 별칭) */
    public static ProductImages fromUrls(List<String> urls) {
        return fromThumbnailUrls(urls);
    }

    /** 상세 설명 URL 목록 추가 */
    public ProductImages addDescriptionImages(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return this;
        }
        List<ProductImage> newImages = new ArrayList<>(this.images);
        int startOrder = getMaxDisplayOrder(ImageType.DESCRIPTION) + 1;
        for (int i = 0; i < urls.size(); i++) {
            newImages.add(ProductImage.description(urls.get(i), startOrder + i));
        }
        return new ProductImages(newImages);
    }

    /** 모든 이미지 반환 (불변) */
    public List<ProductImage> getAll() {
        return Collections.unmodifiableList(images);
    }

    /** 썸네일 이미지만 반환 */
    public List<ProductImage> getThumbnails() {
        return images.stream()
                .filter(ProductImage::isThumbnail)
                .sorted((a, b) -> Integer.compare(a.displayOrder(), b.displayOrder()))
                .collect(Collectors.toList());
    }

    /** 상세 설명 이미지만 반환 */
    public List<ProductImage> getDescriptionImages() {
        return images.stream()
                .filter(ProductImage::isDescription)
                .sorted((a, b) -> Integer.compare(a.displayOrder(), b.displayOrder()))
                .collect(Collectors.toList());
    }

    /** 업로드가 필요한 이미지 반환 */
    public List<ProductImage> getPendingUploads() {
        return images.stream().filter(ProductImage::needsUpload).collect(Collectors.toList());
    }

    /** 업로드가 필요한 이미지의 원본 URL 목록 반환 */
    public List<String> getPendingUploadUrls() {
        return getPendingUploads().stream()
                .map(ProductImage::originalUrl)
                .collect(Collectors.toList());
    }

    /**
     * 특정 원본 URL의 이미지를 S3 업로드 완료 상태로 갱신
     *
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     * @return 갱신된 ProductImages
     */
    public ProductImages updateS3Url(String originalUrl, String s3Url) {
        List<ProductImage> updated =
                images.stream()
                        .map(
                                img ->
                                        img.originalUrl().equals(originalUrl)
                                                ? img.withS3Uploaded(s3Url)
                                                : img)
                        .collect(Collectors.toList());
        return new ProductImages(updated);
    }

    /** 모든 이미지가 업로드 완료되었는지 확인 */
    public boolean allUploaded() {
        return images.stream().allMatch(ProductImage::isUploaded);
    }

    /** 이미지 개수 */
    public int size() {
        return images.size();
    }

    /** 비어있는지 확인 */
    public boolean isEmpty() {
        return images.isEmpty();
    }

    /** 대표 이미지 (첫 번째 썸네일) 반환 */
    public ProductImage getMainImage() {
        return getThumbnails().stream().findFirst().orElse(null);
    }

    /**
     * 대표 이미지의 S3 URL 반환
     *
     * @return S3 URL (없으면 빈 문자열)
     */
    public String getMainImageS3Url() {
        ProductImage mainImage = getMainImage();
        if (mainImage == null) {
            return "";
        }
        String s3Url = mainImage.s3Url();
        return s3Url != null ? s3Url : "";
    }

    /**
     * 이미지 변경 여부 확인 (URL 기준)
     *
     * @param other 비교 대상
     * @return 변경이 있으면 true
     */
    public boolean hasChanges(ProductImages other) {
        if (other == null) {
            return !this.isEmpty();
        }

        List<String> thisUrls =
                this.images.stream()
                        .map(ProductImage::originalUrl)
                        .sorted()
                        .collect(Collectors.toList());

        List<String> otherUrls =
                other.images.stream()
                        .map(ProductImage::originalUrl)
                        .sorted()
                        .collect(Collectors.toList());

        return !thisUrls.equals(otherUrls);
    }

    /**
     * 새로 추가된 이미지 URL 반환
     *
     * @param previous 이전 이미지 목록
     * @return 새로 추가된 URL 목록
     */
    public List<String> getNewImageUrls(ProductImages previous) {
        if (previous == null || previous.isEmpty()) {
            return this.images.stream().map(ProductImage::originalUrl).collect(Collectors.toList());
        }

        List<String> previousUrls =
                previous.images.stream()
                        .map(ProductImage::originalUrl)
                        .collect(Collectors.toList());

        return this.images.stream()
                .map(ProductImage::originalUrl)
                .filter(url -> !previousUrls.contains(url))
                .collect(Collectors.toList());
    }

    private int getMaxDisplayOrder(ImageType type) {
        return images.stream()
                .filter(img -> img.imageType() == type)
                .mapToInt(ProductImage::displayOrder)
                .max()
                .orElse(-1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductImages that = (ProductImages) o;
        return Objects.equals(images, that.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(images);
    }
}
