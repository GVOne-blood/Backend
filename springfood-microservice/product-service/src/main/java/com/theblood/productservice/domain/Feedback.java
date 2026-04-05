package com.theblood.productservice.domain;

import com.theblood.productservice.common.enums.FeedbackType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "feedbacks")
@AttributeOverride(name = "id", column = @Column(name = "feedback_id"))
public class Feedback extends AbstractEntity {

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(name = "user_id")
    private UUID user_id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product-variants-id")
    private UUID productVariantsId;

    @Column(name = "shop-id")
    private UUID shopId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "content")
    private String content;

    @Column(name = "isActive")
    private boolean isActive;

    @Column(name = "feedback-type")
    @Enumerated(EnumType.STRING)
    private FeedbackType feedbackType;

    @Column(name = "feedback-title")
    private String feedbackTitle;

    // cách nhau = , nếu có nhiều ảnh
    @Column(name = "media-file-id")
    private String mediaFileId;
}
