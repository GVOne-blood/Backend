package com.theblood.productservice.resources;

import com.theblood.productservice.domain.Categories;
import com.theblood.productservice.service.AdminCategoryService;
import com.theblood.productservice.service.CategoryService;
import com.theblood.productservice.service.dto.request.AdminCategoryRequest;
import com.theblood.productservice.service.dto.request.CategoryRequest;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Public + shop-owner endpoints for product categories.
 *
 * <p>System-wide categories ({@code shop_id IS NULL}) are exposed publicly so
 * the storefront can render the homepage catalog. Shop-owner endpoints under
 * {@code /me/*} narrow to categories owned by the caller and let them manage
 * their own taxonomy without affecting other shops. Admin endpoints under
 * {@code /admin/*} let platform admins manage system-wide categories and
 * audit shop-owned ones.</p>
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryResource {

    private final CategoryService categoryService;
    private final AdminCategoryService adminCategoryService;

    /** System-wide catalogue — public, used by the home page. */
    @GetMapping("")
    public ResponseData<List<Categories>> listSystemCategories() {
        return new ResponseData<>(200, "OK", categoryService.getSystemCategories());
    }

    /**
     * Categories the authenticated shop owner can attach to products: system
     * categories plus the ones their shop owns. This is the dropdown source
     * for the product create/edit form.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/visible")
    public ResponseData<List<Categories>> listVisible(@AuthenticationPrincipal CustomUserPrincipal user) {
        UUID shopId = parseShopId(user, false);
        return new ResponseData<>(200, "OK", categoryService.getVisibleCategoriesForShop(shopId));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/me")
    public ResponseData<List<Categories>> listMyCategories(@AuthenticationPrincipal CustomUserPrincipal user) {
        UUID shopId = parseShopId(user, true);
        return new ResponseData<>(200, "OK", categoryService.getCategoriesOfShop(shopId));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/me")
    public ResponseEntity<ResponseData<Categories>> createMyCategory(
        @AuthenticationPrincipal CustomUserPrincipal user,
        @Valid @RequestBody CategoryRequest request
    ) {
        UUID shopId = parseShopId(user, true);
        Categories created = categoryService.createForShop(shopId, request);
        return new ResponseEntity<>(new ResponseData<>(201, "Created", created), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/me/{name}")
    public ResponseData<Categories> updateMyCategory(
        @AuthenticationPrincipal CustomUserPrincipal user,
        @PathVariable("name") String name,
        @Valid @RequestBody CategoryRequest request
    ) {
        UUID shopId = parseShopId(user, true);
        Categories updated = categoryService.updateForShop(shopId, name, request);
        return new ResponseData<>(200, "Updated", updated);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/me/{name}")
    public ResponseEntity<ResponseData<Void>> deleteMyCategory(
        @AuthenticationPrincipal CustomUserPrincipal user,
        @PathVariable("name") String name
    ) {
        UUID shopId = parseShopId(user, true);
        categoryService.softDeleteForShop(shopId, name);
        return new ResponseEntity<>(new ResponseData<>(204, "Deleted", null), HttpStatus.NO_CONTENT);
    }

    /**
     * Resolves the caller's shop id; throws 403 when {@code required} is true
     * and the caller doesn't own a shop yet.
     */
    private static UUID parseShopId(CustomUserPrincipal principal, boolean required) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        String sid = principal.getShopId();
        if (sid == null || sid.isBlank() || "null".equalsIgnoreCase(sid)) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own a shop yet.");
            }
            return null;
        }
        try {
            return UUID.fromString(sid);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop id");
        }
    }

    // ---------------------------------------------------------------------
    // Admin endpoints — platform-wide category management.
    //
    // Admin có thể:
    //   - Liệt kê tất cả categories (kể cả inactive, kể cả của mọi shop).
    //   - Tạo system category (shopId null) hoặc category cho 1 shop bất kỳ.
    //   - Sửa, toggle active, hoặc xoá hẳn category.
    // ---------------------------------------------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseData<List<Categories>> listAllForAdmin() {
        return new ResponseData<>(200, "OK", adminCategoryService.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<ResponseData<Categories>> createForAdmin(
        @Valid @RequestBody AdminCategoryRequest request
    ) {
        Categories created = adminCategoryService.createCategory(request);
        return new ResponseEntity<>(new ResponseData<>(201, "Created", created), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{name}")
    public ResponseData<Categories> updateForAdmin(
        @PathVariable("name") String name,
        @Valid @RequestBody AdminCategoryRequest request
    ) {
        Categories updated = adminCategoryService.updateCategory(name, request);
        return new ResponseData<>(200, "Updated", updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{name}/active")
    public ResponseData<Categories> setActive(
        @PathVariable("name") String name,
        @RequestParam("value") boolean active
    ) {
        return new ResponseData<>(200, "Updated", adminCategoryService.setActive(name, active));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{name}")
    public ResponseEntity<ResponseData<Void>> deleteForAdmin(@PathVariable("name") String name) {
        adminCategoryService.deleteCategory(name);
        return new ResponseEntity<>(new ResponseData<>(204, "Deleted", null), HttpStatus.NO_CONTENT);
    }
}
