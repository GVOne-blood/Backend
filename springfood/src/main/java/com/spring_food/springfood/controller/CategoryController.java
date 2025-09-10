package com.spring_food.springfood.controller;


import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.model.Categories;
import com.spring_food.springfood.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<List<Categories>>> getAllCategories() {
        List<Categories> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
                new ResponseData<>(200, "Get all categories successfully", categories)
        );
    }

    @PostMapping("/")
    public ResponseEntity<ResponseData<?>> addCategory(String categoryName, String description) {
        categoryService.addCategory(categoryName, description);
        return ResponseEntity.ok(
                new ResponseData<>(201, "Category added successfully", null)
        );
    }

    @PutMapping("/{categoryName}")
    public ResponseEntity<ResponseData<?>> updateCategory(@PathVariable String categoryName, String description, boolean isActive) {
        categoryService.updateCategory(categoryName, description, isActive);
        return ResponseEntity.ok(
                new ResponseData<>(200, "Category updated successfully", null)
        );
    }

    @DeleteMapping("/{categoryName}")
    public ResponseEntity<ResponseData<?>> deleteCategory(@PathVariable String categoryName) {
        categoryService.deleteCategory(categoryName);
        return ResponseEntity.ok(
                new ResponseData<>(204, "Category deleted successfully", null)
        );
}
}
