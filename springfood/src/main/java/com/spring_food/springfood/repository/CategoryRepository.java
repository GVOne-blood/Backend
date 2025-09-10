package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Categories, String>{

    List<Categories> findAll();
    boolean existsById(String categoryName);
    Optional<Categories> findById(String categoryName);
    //Optional<Set<Categories>> findAllById(Set<String> categoryNames);
}
