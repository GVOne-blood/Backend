package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.PostStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "post")
@AttributeOverride(name = "id", column = @Column(name = "post_id"))
public class Post extends AbstractEntity {
    
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status")
    private PostStatus postStatus;
}
