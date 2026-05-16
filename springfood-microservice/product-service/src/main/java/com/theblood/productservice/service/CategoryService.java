package com.theblood.productservice.service;

import com.theblood.productservice.domain.Categories;
import com.theblood.productservice.repository.CategoryRepository;
import com.theblood.productservice.service.dto.request.CategoryRequest;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Service exposing read access to system-wide categories and full CRUD over
 * the categories owned by a given shop. The {@code shop_id} column on
 * {@code categories} is the authoritative owner record:
 *
 * <ul>
 *   <li>{@code shop_id IS NULL} → system category, read-only for shop owners.</li>
 *   <li>{@code shop_id = X}    → shop-owned category, only that shop can mutate.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Categories> getSystemCategories() {
        return categoryRepository.findSystemCategories();
    }

    public List<Categories> getCategoriesOfShop(UUID shopId) {
        if (shopId == null) {
            throw new InvalidDataException("shopId is required");
        }
        return categoryRepository.findByShopId(shopId);
    }

    public List<Categories> getVisibleCategoriesForShop(UUID shopId) {
        if (shopId == null) {
            return getSystemCategories();
        }
        return categoryRepository.findVisibleForShop(shopId);
    }

    public Categories createForShop(UUID shopId, CategoryRequest request) {
        if (shopId == null) {
            throw new InvalidDataException("shopId is required");
        }
        if (categoryRepository.existsById(request.getName())) {
            throw new InvalidDataException("Category name already exists: " + request.getName());
        }

        Categories entity = new Categories();
        entity.setName(request.getName());
        entity.setSlug(StringUtils.hasText(request.getSlug()) ? request.getSlug() : slugify(request.getName()));
        entity.setDescription(request.getDescription());
        entity.setActive(true);
        entity.setShopId(shopId);
        entity.setCategoryGroupCode(request.getCategoryGroupCode());

        if (StringUtils.hasText(request.getParentName())) {
            Categories parent = categoryRepository.findById(request.getParentName())
                .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.getParentName()));
            // Disallow assigning a parent the shop doesn't have visibility on.
            if (parent.getShopId() != null && !parent.getShopId().equals(shopId)) {
                throw new InvalidDataException("Cannot use category '" + parent.getName() + "' as parent");
            }
            entity.setParentCategories(parent);
        }

        return categoryRepository.save(entity);
    }

    public Categories updateForShop(UUID shopId, String name, CategoryRequest request) {
        Categories existing = loadOwnedCategory(shopId, name);
        if (StringUtils.hasText(request.getSlug())) existing.setSlug(request.getSlug());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getCategoryGroupCode() != null) existing.setCategoryGroupCode(request.getCategoryGroupCode());

        if (request.getParentName() != null) {
            if (request.getParentName().isBlank()) {
                existing.setParentCategories(null);
            } else {
                Categories parent = categoryRepository.findById(request.getParentName())
                    .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.getParentName()));
                if (parent.getShopId() != null && !parent.getShopId().equals(shopId)) {
                    throw new InvalidDataException("Cannot use category '" + parent.getName() + "' as parent");
                }
                if (parent.getName().equals(existing.getName())) {
                    throw new InvalidDataException("A category cannot be its own parent");
                }
                existing.setParentCategories(parent);
            }
        }
        return categoryRepository.save(existing);
    }

    /** Soft-delete: flips {@code is_active} to false so referenced products keep their FK. */
    public void softDeleteForShop(UUID shopId, String name) {
        Categories existing = loadOwnedCategory(shopId, name);
        existing.setActive(false);
        categoryRepository.save(existing);
    }

    private Categories loadOwnedCategory(UUID shopId, String name) {
        Categories existing = categoryRepository.findById(name)
            .orElseThrow(() -> new NotFoundException("Category not found: " + name));
        if (existing.getShopId() == null) {
            throw new InvalidDataException("System categories cannot be modified by shop owners.");
        }
        if (!existing.getShopId().equals(shopId)) {
            throw new InvalidDataException("Category does not belong to your shop.");
        }
        return existing;
    }

    private static String slugify(String value) {
        if (value == null) return null;
        return value
            .trim()
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-");
    }
}
