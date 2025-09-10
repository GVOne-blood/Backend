package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.model.Categories;
import com.spring_food.springfood.repository.CategoryRepository;
import com.spring_food.springfood.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    @Override
    public List<Categories> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public void addCategory(String categoryName, String description) {
        Categories categories = new Categories();
        categories.setCategoryName(categoryName);
        categories.setDescription(description);
        categories.setActive(true);

        categoryRepository.save(categories);
    }

    @Override
    public void updateCategory(String categoryName, String description, boolean isActive) {
        Optional<Categories> categories = categoryRepository.findById(categoryName);
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Category not found");
        }

        if (!categoryName.isBlank()) categories.get().setCategoryName(categoryName);
        if (!description.isBlank()) categories.get().setDescription(description);
        categories.get().setActive(isActive);
        categoryRepository.save(categories.get());
    }

    @Override
    public void deleteCategory(String categoryName) {
            if (!categoryRepository.existsById(categoryName)) {
                throw new IllegalArgumentException("Category not found");
            }
            categoryRepository.deleteById(categoryName);
    }
}
