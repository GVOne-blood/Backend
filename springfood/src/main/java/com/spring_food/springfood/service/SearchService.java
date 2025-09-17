package com.spring_food.springfood.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SearchService<T> {
        Page<T> search(Pageable pageable, String[] params);
}
