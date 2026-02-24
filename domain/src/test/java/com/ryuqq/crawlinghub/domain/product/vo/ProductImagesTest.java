package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductImages 일급 컬렉션 단위 테스트")
class ProductImagesTest {

    private ProductImage thumbnailImage(String url, int order) {
        return ProductImage.thumbnail(url, order);
    }

    private ProductImage descriptionImage(String url, int order) {
        return ProductImage.description(url, order);
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("빈 컬렉션을 생성한다")
        void createEmptyImages() {
            ProductImages images = ProductImages.empty();
            assertThat(images.isEmpty()).isTrue();
            assertThat(images.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("이미지 목록으로 생성한다")
        void createWithImages() {
            List<ProductImage> imageList =
                    List.of(
                            thumbnailImage("https://img.com/1.jpg", 0),
                            thumbnailImage("https://img.com/2.jpg", 1));

            ProductImages images = ProductImages.of(imageList);

            assertThat(images.size()).isEqualTo(2);
            assertThat(images.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("썸네일 URL 목록으로 생성한다")
        void createFromThumbnailUrls() {
            List<String> urls = List.of("https://img.com/1.jpg", "https://img.com/2.jpg");

            ProductImages images = ProductImages.fromThumbnailUrls(urls);

            assertThat(images.size()).isEqualTo(2);
            assertThat(images.getThumbnails()).hasSize(2);
        }

        @Test
        @DisplayName("URL 목록이 null이면 빈 컬렉션을 생성한다")
        void createFromNullUrlsReturnsEmpty() {
            ProductImages images = ProductImages.fromThumbnailUrls(null);
            assertThat(images.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fromUrls는 fromThumbnailUrls와 동일하다")
        void fromUrlsIsAliasForFromThumbnailUrls() {
            List<String> urls = List.of("https://img.com/1.jpg");
            ProductImages images = ProductImages.fromUrls(urls);
            assertThat(images.size()).isEqualTo(1);
            assertThat(images.getThumbnails()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("이미지 조회 테스트")
    class ImageRetrievalTest {

        @Test
        @DisplayName("썸네일 이미지만 반환한다")
        void returnsThumbnailsOnly() {
            ProductImages images =
                    ProductImages.of(
                            List.of(
                                    thumbnailImage("https://img.com/thumb.jpg", 0),
                                    descriptionImage("https://img.com/desc.jpg", 0)));

            assertThat(images.getThumbnails()).hasSize(1);
            assertThat(images.getThumbnails().get(0).isThumbnail()).isTrue();
        }

        @Test
        @DisplayName("상세 설명 이미지만 반환한다")
        void returnsDescriptionImagesOnly() {
            ProductImages images =
                    ProductImages.of(
                            List.of(
                                    thumbnailImage("https://img.com/thumb.jpg", 0),
                                    descriptionImage("https://img.com/desc.jpg", 0)));

            assertThat(images.getDescriptionImages()).hasSize(1);
            assertThat(images.getDescriptionImages().get(0).isDescription()).isTrue();
        }

        @Test
        @DisplayName("대표 이미지(첫 번째 썸네일)를 반환한다")
        void returnsMainImage() {
            ProductImages images =
                    ProductImages.of(
                            List.of(
                                    thumbnailImage("https://img.com/first.jpg", 0),
                                    thumbnailImage("https://img.com/second.jpg", 1)));

            assertThat(images.getMainImage()).isNotNull();
            assertThat(images.getMainImage().originalUrl()).isEqualTo("https://img.com/first.jpg");
        }

        @Test
        @DisplayName("이미지가 없으면 대표 이미지는 null이다")
        void returnsNullMainImageWhenEmpty() {
            ProductImages images = ProductImages.empty();
            assertThat(images.getMainImage()).isNull();
        }

        @Test
        @DisplayName("대표 이미지의 S3 URL을 반환한다")
        void returnsMainImageS3Url() {
            ProductImage uploaded =
                    ProductImage.thumbnail("https://img.com/1.jpg", 0)
                            .withS3Uploaded("https://s3.com/1.jpg");
            ProductImages images = ProductImages.of(List.of(uploaded));

            assertThat(images.getMainImageS3Url()).isEqualTo("https://s3.com/1.jpg");
        }

        @Test
        @DisplayName("S3 URL이 없으면 빈 문자열을 반환한다")
        void returnsEmptyStringWhenNoS3Url() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            assertThat(images.getMainImageS3Url()).isEmpty();
        }
    }

    @Nested
    @DisplayName("업로드 관련 테스트")
    class UploadTest {

        @Test
        @DisplayName("업로드가 필요한 이미지 URL 목록을 반환한다")
        void returnsPendingUploadUrls() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(
                            List.of("https://img.com/1.jpg", "https://img.com/2.jpg"));

            List<String> pendingUrls = images.getPendingUploadUrls();

            assertThat(pendingUrls).hasSize(2);
        }

        @Test
        @DisplayName("S3 URL을 업데이트한다")
        void updatesS3Url() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            ProductImages updated =
                    images.updateS3Url("https://img.com/1.jpg", "https://s3.com/1.jpg");

            assertThat(updated.getMainImage().s3Url()).isEqualTo("https://s3.com/1.jpg");
        }

        @Test
        @DisplayName("모든 이미지가 업로드 완료되면 allUploaded()가 true를 반환한다")
        void allUploadedReturnsTrueWhenAllCompleted() {
            ProductImage uploaded =
                    ProductImage.thumbnail("https://img.com/1.jpg", 0)
                            .withS3Uploaded("https://s3.com/1.jpg");
            ProductImages images = ProductImages.of(List.of(uploaded));

            assertThat(images.allUploaded()).isTrue();
        }

        @Test
        @DisplayName("일부 이미지가 업로드 미완료이면 allUploaded()가 false를 반환한다")
        void allUploadedReturnsFalseWhenSomePending() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            assertThat(images.allUploaded()).isFalse();
        }
    }

    @Nested
    @DisplayName("상세 이미지 관리 테스트")
    class DescriptionImageManagementTest {

        @Test
        @DisplayName("상세 이미지를 추가한다")
        void addsDescriptionImages() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/thumb.jpg"));

            ProductImages updated =
                    images.addDescriptionImages(List.of("https://img.com/desc.jpg"));

            assertThat(updated.getDescriptionImages()).hasSize(1);
        }

        @Test
        @DisplayName("상세 이미지를 교체한다")
        void replacesDescriptionImages() {
            ProductImages images =
                    ProductImages.of(
                            List.of(
                                    thumbnailImage("https://img.com/thumb.jpg", 0),
                                    descriptionImage("https://img.com/old_desc.jpg", 0)));

            ProductImages updated =
                    images.replaceDescriptionImages(List.of("https://img.com/new_desc.jpg"));

            assertThat(updated.getDescriptionImages()).hasSize(1);
            assertThat(updated.getDescriptionImages().get(0).originalUrl())
                    .isEqualTo("https://img.com/new_desc.jpg");
            assertThat(updated.getThumbnails()).hasSize(1);
        }

        @Test
        @DisplayName("새로 추가된 상세 이미지 URL을 반환한다")
        void returnsNewDescriptionImageUrls() {
            ProductImages images =
                    ProductImages.of(List.of(descriptionImage("https://img.com/existing.jpg", 0)));

            List<String> newUrls =
                    images.getNewDescriptionImageUrls(
                            List.of("https://img.com/existing.jpg", "https://img.com/new.jpg"));

            assertThat(newUrls).containsExactly("https://img.com/new.jpg");
        }

        @Test
        @DisplayName("상세 이미지 변경 여부를 확인한다")
        void checksDescriptionImageChanges() {
            ProductImages images =
                    ProductImages.of(List.of(descriptionImage("https://img.com/desc.jpg", 0)));

            assertThat(images.hasDescriptionImageChanges(List.of("https://img.com/different.jpg")))
                    .isTrue();
            assertThat(images.hasDescriptionImageChanges(List.of("https://img.com/desc.jpg")))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("hasChanges() 테스트")
    class HasChangesTest {

        @Test
        @DisplayName("같은 이미지 URL이면 변경이 없다")
        void sameUrlsHaveNoChanges() {
            ProductImages images1 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages images2 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            assertThat(images1.hasChanges(images2)).isFalse();
        }

        @Test
        @DisplayName("다른 이미지 URL이면 변경이 있다")
        void differentUrlsHaveChanges() {
            ProductImages images1 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages images2 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/2.jpg"));

            assertThat(images1.hasChanges(images2)).isTrue();
        }

        @Test
        @DisplayName("null이면 비어있지 않은 경우 변경이 있다")
        void nullHasChangesWhenNotEmpty() {
            ProductImages images =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            assertThat(images.hasChanges(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAll() 테스트")
    class GetAllTest {

        @Test
        @DisplayName("모든 이미지를 불변 목록으로 반환한다")
        void returnsAllImagesAsUnmodifiableList() {
            List<ProductImage> imageList =
                    List.of(
                            thumbnailImage("https://img.com/thumb.jpg", 0),
                            descriptionImage("https://img.com/desc.jpg", 0));
            ProductImages images = ProductImages.of(imageList);

            List<ProductImage> all = images.getAll();

            assertThat(all).hasSize(2);
            assertThatThrownBy(() -> all.add(thumbnailImage("https://img.com/new.jpg", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("빈 컬렉션이면 빈 불변 목록을 반환한다")
        void returnsEmptyUnmodifiableListWhenEmpty() {
            ProductImages images = ProductImages.empty();

            List<ProductImage> all = images.getAll();

            assertThat(all).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNewImageUrls() 테스트")
    class GetNewImageUrlsTest {

        @Test
        @DisplayName("이전 이미지가 null이면 모든 현재 이미지 URL을 반환한다")
        void returnsAllWhenPreviousIsNull() {
            ProductImages current =
                    ProductImages.fromThumbnailUrls(
                            List.of("https://img.com/1.jpg", "https://img.com/2.jpg"));

            List<String> newUrls = current.getNewImageUrls(null);

            assertThat(newUrls).hasSize(2);
        }

        @Test
        @DisplayName("이전 이미지가 비어있으면 모든 현재 이미지 URL을 반환한다")
        void returnsAllWhenPreviousIsEmpty() {
            ProductImages current =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            List<String> newUrls = current.getNewImageUrls(ProductImages.empty());

            assertThat(newUrls).containsExactly("https://img.com/1.jpg");
        }

        @Test
        @DisplayName("이전에 없던 이미지 URL만 반환한다")
        void returnsOnlyNewImageUrls() {
            ProductImages previous =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages current =
                    ProductImages.fromThumbnailUrls(
                            List.of("https://img.com/1.jpg", "https://img.com/2.jpg"));

            List<String> newUrls = current.getNewImageUrls(previous);

            assertThat(newUrls).containsExactly("https://img.com/2.jpg");
        }

        @Test
        @DisplayName("모두 기존 이미지이면 빈 목록을 반환한다")
        void returnsEmptyWhenNoNewImages() {
            ProductImages previous =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages current =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));

            List<String> newUrls = current.getNewImageUrls(previous);

            assertThat(newUrls).isEmpty();
        }
    }

    @Nested
    @DisplayName("addDescriptionImages() null/empty 처리 테스트")
    class AddDescriptionImagesNullTest {

        @Test
        @DisplayName("null URL 목록을 추가하면 원본이 반환된다")
        void returnsOriginalWhenUrlsIsNull() {
            ProductImages original =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages result = original.addDescriptionImages(null);
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("빈 URL 목록을 추가하면 원본이 반환된다")
        void returnsOriginalWhenUrlsIsEmpty() {
            ProductImages original =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages result = original.addDescriptionImages(List.of());
            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("replaceDescriptionImages() null 처리 테스트")
    class ReplaceDescriptionImagesNullTest {

        @Test
        @DisplayName("null URL 목록으로 교체하면 썸네일만 남는다")
        void keepsThumbnailsWhenUrlsIsNull() {
            ProductImages images =
                    ProductImages.of(
                            List.of(
                                    thumbnailImage("https://img.com/thumb.jpg", 0),
                                    descriptionImage("https://img.com/desc.jpg", 0)));
            ProductImages result = images.replaceDescriptionImages(null);
            assertThat(result.getThumbnails()).hasSize(1);
            assertThat(result.getDescriptionImages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 이미지 목록이면 동일하다")
        void sameImagesAreEqual() {
            List<ProductImage> imageList = List.of(thumbnailImage("https://img.com/1.jpg", 0));
            ProductImages images1 = ProductImages.of(imageList);
            ProductImages images2 = ProductImages.of(imageList);

            assertThat(images1).isEqualTo(images2);
            assertThat(images1.hashCode()).isEqualTo(images2.hashCode());
        }

        @Test
        @DisplayName("다른 이미지 목록이면 다르다")
        void differentImagesAreNotEqual() {
            ProductImages images1 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/1.jpg"));
            ProductImages images2 =
                    ProductImages.fromThumbnailUrls(List.of("https://img.com/2.jpg"));

            assertThat(images1).isNotEqualTo(images2);
        }
    }
}
