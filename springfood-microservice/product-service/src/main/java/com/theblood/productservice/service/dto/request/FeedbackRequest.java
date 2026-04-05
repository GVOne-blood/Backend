package com.theblood.productservice.service.dto.request;

import com.theblood.productservice.common.enums.FeedbackType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {

    private UUID shopId;
    private UUID productVariantsId;
    private UUID productId;

    FeedbackType type;
    private Integer rating;
    private String mediaFileId;
    private String content;
    private boolean isShopReply;


}
