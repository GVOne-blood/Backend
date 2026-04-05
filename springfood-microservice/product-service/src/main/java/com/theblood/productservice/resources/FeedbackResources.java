package com.theblood.productservice.resources;

import com.theblood.productservice.service.FeedbackService;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackResources {

    FeedbackService feedbackService;

    @GetMapping("/shop")
    public ResponseEntity<?> getShopFeedback(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam String shopId) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        return ResponseEntity.ok().body(feedbackService.getShopFeedback(pageable, shopId));
    }


    @GetMapping("/product")
    public ResponseEntity<?> getProductFeedback(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam String productId) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        return ResponseEntity.ok().body(feedbackService.getProductFeedback(pageable, productId));
    }

    @PostMapping("")
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        FeedbackResponse res = feedbackService.createFeedback(feedbackRequest);
        return ResponseEntity.ok().body(res);
    }

    @PutMapping("")
    public ResponseEntity<?> updateFeedback(
            @RequestBody FeedbackRequest feedbackRequest
    ) {
        return ResponseEntity.ok().body(feedbackService.updateFeedback(feedbackRequest));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteFeedback(@RequestBody List<String> feedbackIds) {
        return ResponseEntity.ok().body(feedbackService.deleteFeedback(feedbackIds));
    }
}
