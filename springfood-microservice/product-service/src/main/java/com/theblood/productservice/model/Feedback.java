package com.theblood.productservice.model;

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

    private Integer rating;

    private String content;
}
