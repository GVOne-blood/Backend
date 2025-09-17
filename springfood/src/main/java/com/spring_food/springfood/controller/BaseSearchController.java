package com.spring_food.springfood.controller;


import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.service.SearchService;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@MappedSuperclass
public abstract class BaseSearchController<T, S extends SearchService<T>> { // Response

    protected S service;

    protected BaseSearchController(S service) {
        this.service = service;
    }

    protected BaseSearchController() {
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<T>>> search(
            @PageableDefault(page = 0, size = 5, sort = "updated_at", direction = Sort.Direction.ASC) Pageable pageable,
            @ModelAttribute String... params) {

       Page<T> result = service.search(pageable, params);

        return ResponseEntity.ok(new ResponseData<>(200, "Search finished ", result));
    }
}