package com.theblood.productservice.service.impl;

import com.theblood.productservice.common.enums.FeedbackType;
import com.theblood.productservice.domain.Feedback;
import com.theblood.productservice.repository.FeedbackRepository;
import com.theblood.productservice.service.FeedbackService;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackDeleteResponse;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import com.theblood.productservice.service.mapper.FeedbackMapper;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    FeedbackRepository feedbackRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    FeedbackMapper feedbackMapper;


    @Override
    public Page<FeedbackResponse> getShopFeedback(Pageable pageable, String shopId) {
        if (shopId == null || shopId.isBlank()) {
            throw new InvalidDataException("shopId is required");
        }
        try {
            Page<Feedback> feedbacks = feedbackRepository.findAllByShopId(pageable, UUID.fromString(shopId));
            return feedbacks.map(feedbackMapper::toDto);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("shopId is not a valid UUID: " + shopId);
        }
    }

    @Override
    public Page<FeedbackResponse> getProductFeedback(Pageable pageable, String productId) {
        if (productId == null || productId.isBlank()) {
            throw new InvalidDataException("productId is required");
        }
        try {
            Page<Feedback> feedbacks = feedbackRepository.findAllByProductId(pageable, UUID.fromString(productId));
            return feedbacks.map(feedbackMapper::toDto);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("productId is not a valid UUID: " + productId);
        }
    }

    @Transactional
    @Override
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        if (feedbackRequest.getProductId() == null) {
            throw new InvalidDataException("productId is required");
        }
        if (feedbackRequest.getRating() == null
                || feedbackRequest.getRating() < 1
                || feedbackRequest.getRating() > 5) {
            throw new InvalidDataException("Rating must be between 1 and 5");
        }
        if (feedbackRequest.getContent() == null || feedbackRequest.getContent().isBlank()) {
            throw new InvalidDataException("Content is required");
        }

        // Resolve current user from the per-request context populated by the
        // InternalAuthenticationFilter (which copies headers injected by the
        // gateway into UserContextHolder).
        CustomUserPrincipal principal = UserContextHolder.getContext();
        UUID currentUserId = principal != null ? principal.getUserId() : null;
        if (currentUserId == null) {
            throw new InvalidDataException("Authentication required to submit feedback");
        }

        // Default the feedback type to PRODUCT_FEEDBACK for the public
        // product detail modal flow when caller doesn't set it explicitly.
        if (feedbackRequest.getType() == null) {
            feedbackRequest.setType(FeedbackType.PRODUCT_FEEDBACK);
        }

        Feedback feedback = feedbackMapper.toEntity(feedbackRequest);
        feedback.setUser_id(currentUserId);
        feedback.setActive(true);
        feedback = feedbackRepository.save(feedback);

        FeedbackResponse res = feedbackMapper.toDto(feedback);

        // Emit kafka event so background jobs can recompute average rating
        // for the product/shop. Failure must NOT block the comment from
        // being persisted.
        try {
            calculateStar(res);
        } catch (Exception e) {
            log.warn("Failed to publish feedback event (non-fatal): {}", e.getMessage());
        }

        return res;
    }

    @Transactional
    @Override
    public FeedbackResponse updateFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackRepository.findById(UUID.fromString(feedbackRequest.getProductId().toString()))
                .orElseThrow(() -> new NotFoundException("Feedback not found with id: " + feedbackRequest.getProductId()));

        // Authorise: only the original author can edit their own comment.
        CustomUserPrincipal principal = UserContextHolder.getContext();
        UUID currentUserId = principal != null ? principal.getUserId() : null;
        if (currentUserId == null || !currentUserId.equals(feedback.getUser_id())) {
            throw new InvalidDataException("You are not allowed to edit this feedback");
        }

        if (feedbackRequest.getContent() != null) feedback.setContent(feedbackRequest.getContent());
        if (feedbackRequest.getRating() != null) feedback.setRating(feedbackRequest.getRating());
        if (feedbackRequest.getMediaFileId() != null) feedback.setMediaFileId(feedbackRequest.getMediaFileId());
        feedbackRepository.save(feedback);
        return feedbackMapper.toDto(feedback);
    }

    @Transactional
    @Override
    public FeedbackDeleteResponse deleteFeedback(List<String> feedbackIds) {
        if (feedbackIds == null || feedbackIds.isEmpty()) {
            return FeedbackDeleteResponse.builder()
                    .deleteCount(0)
                    .success(0)
                    .message("No feedback ids provided")
                    .build();
        }

        CustomUserPrincipal principal = UserContextHolder.getContext();
        UUID currentUserId = principal != null ? principal.getUserId() : null;

        List<Feedback> feedbacks = feedbackRepository.findByIds(feedbackIds);
        AtomicInteger deleteCount = new AtomicInteger();
        feedbacks.forEach(feedback -> {
            // Soft delete only the user's own feedback rows. Bulk delete
            // requests targeting other users' rows are silently skipped.
            if (currentUserId != null && currentUserId.equals(feedback.getUser_id())) {
                feedback.setActive(false);
                feedbackRepository.save(feedback);
                deleteCount.getAndIncrement();
            }
        });

        return FeedbackDeleteResponse.builder()
                .deleteCount(feedbackIds.size())
                .success(deleteCount.get())
                .message("Deleted " + deleteCount.get() + " out of " + feedbackIds.size() + " feedback(s)")
                .build();
    }

    private void calculateStar(FeedbackResponse feedback) {
        if (feedback.getFeedbackType() == null) return;
        if (feedback.getFeedbackType().equals(FeedbackType.SHOP_FEEDBACK)) {
            kafkaTemplate.send("shop-feedback", feedback.getShopId(), feedback);
        } else if (feedback.getFeedbackType().equals(FeedbackType.PRODUCT_FEEDBACK)) {
            kafkaTemplate.send("product-feedback", feedback.getProductId(), feedback);
        }
    }
}
