package com.spring_food.springfood.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SearchCriteria {

    String keyword;
    String operation;
    String value;
}
