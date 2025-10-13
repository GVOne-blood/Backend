package com.theblood.productservice.repository;

import com.theblood.productservice.model.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, String> {

    List<Categories> findAll();

    boolean existsById(String categoryName);

    Optional<Categories> findById(String categoryName);


    Page<Categories> findAllByName(String name, Pageable pageable);

}
