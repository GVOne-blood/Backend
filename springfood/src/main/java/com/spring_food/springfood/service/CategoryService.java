package com.spring_food.springfood.service;

import com.spring_food.springfood.model.Categories;

import java.util.List;

public interface CategoryService {

    List<Categories> getAllCategories();
    void addCategory(String categoryName, String description);
    void updateCategory(String categoryName, String description, boolean isActive);
    void deleteCategory(String categoryName);
}
