package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.TokenType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Token {
    @Id
    private String token;

    private String userId;

    private TokenType tokenType;
}
