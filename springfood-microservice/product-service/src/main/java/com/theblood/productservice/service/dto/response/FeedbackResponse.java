package com.theblood.productservice.service.dto.response;

import com.theblood.productservice.common.enums.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FeedbackResponse {

    String shopId;
    String productId;
    String productVariantsId;
    String mediaFileId;
    String createdAt;
    String createdBy;
    String updatedAt;
    String updatedBy;
    String content;
    Integer rate;
    FeedbackType feedbackType;
    String feedbackTitle;

}
