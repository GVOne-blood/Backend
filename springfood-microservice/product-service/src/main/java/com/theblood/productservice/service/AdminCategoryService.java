package com.theblood.productservice.service;

import com.theblood.productservice.domain.Categories;
import com.theblood.productservice.repository.CategoryRepository;
import com.theblood.productservice.service.dto.request.AdminCategoryRequest;
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
 * Admin-only service cho category. Khác {@link CategoryService} (shop owner),
 * service này:
 *
 * <ul>
 *   <li>Liệt kê được mọi category của mọi shop, kể cả inactive.</li>
 *   <li>Cho phép tạo system category ({@code shopId IS NULL}).</li>
 *   <li>Cho phép sửa cả system + shop-owned category.</li>
 *   <li>Có thể activate/deactivate (toggle {@code is_active}).</li>
 * </ul>
 *
 * <p>Authorization được enforce ở controller bằng {@code @PreAuthorize("hasRole('ADMIN')")}.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    /** Mọi category — dùng cho admin list page. */
    public List<Categories> listAll() {
        // findAll() tự sort theo name ASC do JPA default; ta thêm filter sort sau ở FE.
        return categoryRepository.findAll();
    }

    public Categories createCategory(AdminCategoryRequest request) {
        if (categoryRepository.existsById(request.getName())) {
            throw new InvalidDataException("Category name already exists: " + request.getName());
        }

        Categories entity = new Categories();
        entity.setName(request.getName());
        entity.setSlug(StringUtils.hasText(request.getSlug()) ? request.getSlug() : slugify(request.getName()));
        entity.setDescription(request.getDescription());
        entity.setActive(true);
        entity.setShopId(parseShopIdOrNull(request.getShopId()));
        entity.setCategoryGroupCode(request.getCategoryGroupCode());

        if (StringUtils.hasText(request.getParentName())) {
            Categories parent = categoryRepository.findById(request.getParentName())
                .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.getParentName()));
            entity.setParentCategories(parent);
        }
        return categoryRepository.save(entity);
    }

    public Categories updateCategory(String name, AdminCategoryRequest request) {
        Categories existing = categoryRepository.findById(name)
            .orElseThrow(() -> new NotFoundException("Category not found: " + name));

        if (StringUtils.hasText(request.getSlug())) existing.setSlug(request.getSlug());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getCategoryGroupCode() != null) existing.setCategoryGroupCode(request.getCategoryGroupCode());

        // Cho phép admin reassign owner (vd: convert system → shop, hoặc transfer giữa shop).
        if (request.getShopId() != null) {
            existing.setShopId(parseShopIdOrNull(request.getShopId()));
        }

        if (request.getParentName() != null) {
            if (request.getParentName().isBlank()) {
                existing.setParentCategories(null);
            } else {
                Categories parent = categoryRepository.findById(request.getParentName())
                    .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.getParentName()));
                if (parent.getName().equals(existing.getName())) {
                    throw new InvalidDataException("A category cannot be its own parent");
                }
                existing.setParentCategories(parent);
            }
        }
        return categoryRepository.save(existing);
    }

    /** Toggle is_active. Dùng cho cả enable/disable. */
    public Categories setActive(String name, boolean active) {
        Categories existing = categoryRepository.findById(name)
            .orElseThrow(() -> new NotFoundException("Category not found: " + name));
        existing.setActive(active);
        return categoryRepository.save(existing);
    }

    /**
     * Hard delete — chỉ dùng cho category chưa có sản phẩm tham chiếu. Nếu DB
     * có FK products→categories với ON DELETE RESTRICT, JPA sẽ throw
     * DataIntegrityViolationException và FE phải hiển thị thông báo phù hợp.
     */
    public void deleteCategory(String name) {
        Categories existing = categoryRepository.findById(name)
            .orElseThrow(() -> new NotFoundException("Category not found: " + name));
        categoryRepository.delete(existing);
    }

    private static UUID parseShopIdOrNull(String shopId) {
        if (shopId == null || shopId.isBlank() || "null".equalsIgnoreCase(shopId)) return null;
        try {
            return UUID.fromString(shopId);
        } catch (IllegalArgumentException ex) {
            throw new InvalidDataException("Invalid shopId: " + shopId);
        }
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
