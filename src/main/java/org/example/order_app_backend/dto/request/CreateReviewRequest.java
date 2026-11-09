package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Điểm đánh giá quán ăn không được để trống")
    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5 sao")
    private Integer storeRating;

    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5 sao")
    @Builder.Default
    private Integer shipperRating = 5;

    // Cmt hoặc ảnh đều được, có cmt ko có ảnh cũng dc, ko có cmt mà có ảnh cũng dc, không ràng buộc
    private String comment;

    private String shipperComment;

    // Danh sách URL ảnh minh chứng (tối đa 3 ảnh)
    private List<String> images;
}
