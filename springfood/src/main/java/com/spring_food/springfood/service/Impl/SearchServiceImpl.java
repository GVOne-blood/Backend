package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl<T> implements SearchService<T> {


    @Override
    public Page<T> search(Pageable pageable, String[] params) {
        return null;
    }
}
