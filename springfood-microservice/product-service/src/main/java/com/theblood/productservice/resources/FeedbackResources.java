package com.theblood.productservice.resources;

import com.theblood.productservice.service.FeedbackService;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackDeleteResponse;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public + authenticated REST endpoints for product/shop feedback (reviews).
 *
 * <ul>
 *   <li>{@code GET /feedback/product?productId=…}  — public, paginated</li>
 *   <li>{@code GET /feedback/shop?shopId=…}        — public, paginated</li>
 *   <li>{@code POST /feedback}                     — authenticated; userId
 *       resolved from {@code UserContextHolder}</li>
 *   <li>{@code PUT /feedback}                      — author only</li>
 *   <li>{@code DELETE /feedback}                   — author only (soft-delete by ids)</li>
 * </ul>
 *
 * Responses are wrapped in {@link ResponseData} so the FE keeps the same
 * envelope shape it already uses for products / shop / cart.
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackResources {

    FeedbackService feedbackService;

    @GetMapping("/shop")
    public ResponseEntity<ResponseData<Page<FeedbackResponse>>> getShopFeedback(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam String shopId) {
        Page<FeedbackResponse> page = feedbackService.getShopFeedback(pageable, shopId);
        return ResponseEntity.ok(new ResponseData<>(200, "Get shop feedback successfully", page));
    }

    @GetMapping("/product")
    public ResponseEntity<ResponseData<Page<FeedbackResponse>>> getProductFeedback(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam String productId) {
        Page<FeedbackResponse> page = feedbackService.getProductFeedback(pageable, productId);
        return ResponseEntity.ok(new ResponseData<>(200, "Get product feedback successfully", page));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public ResponseEntity<ResponseData<FeedbackResponse>> createFeedback(
            @Valid @RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse res = feedbackService.createFeedback(feedbackRequest);
        return new ResponseEntity<>(
                new ResponseData<>(201, "Create feedback successfully", res),
                HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("")
    public ResponseEntity<ResponseData<FeedbackResponse>> updateFeedback(
            @RequestBody FeedbackRequest feedbackRequest) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Update feedback successfully",
                        feedbackService.updateFeedback(feedbackRequest)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("")
    public ResponseEntity<ResponseData<FeedbackDeleteResponse>> deleteFeedback(
            @RequestBody List<String> feedbackIds) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Delete feedback successfully",
                        feedbackService.deleteFeedback(feedbackIds)));
    }
}
