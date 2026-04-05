package com.theblood.productservice.service.impl;

import com.theblood.productservice.common.enums.FeedbackType;
import com.theblood.productservice.domain.Feedback;
import com.theblood.productservice.repository.FeedbackRepository;
import com.theblood.productservice.service.FeedbackService;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackDeleteResponse;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import com.theblood.productservice.service.mapper.FeedbackMapper;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
public class FeedbackServiceImpl implements FeedbackService {

    FeedbackRepository feedbackRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    FeedbackMapper feedbackMapper;


    @Override
    public Page<FeedbackResponse> getShopFeedback(Pageable pageable, String shopId) {


        Page<Feedback> feedbacks = feedbackRepository.findAllByShopId(pageable, UUID.fromString(shopId));

        return feedbacks.map(feedbackMapper::toDto);
    }

    @Override
    public Page<FeedbackResponse> getProductFeedback(Pageable pageable, String productId) {
        Page<Feedback> feedbacks = feedbackRepository.findAllByProductId(pageable, UUID.fromString(productId));

        return feedbacks.map(feedbackMapper::toDto);

    }

    @Transactional
    @Override
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackMapper.toEntity(feedbackRequest);
        feedback = feedbackRepository.save(feedback);
        FeedbackResponse res = feedbackMapper.toDto(feedback);
        return res;
    }

    @Transactional
    @Override
    public FeedbackResponse updateFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackRepository.findById(UUID.fromString(feedbackRequest.getProductId().toString()))
                .orElseThrow(() -> new NotFoundException("Feedback not found with id: " + feedbackRequest.getProductId()));
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setMediaFileId(feedbackRequest.getMediaFileId());
        feedbackRepository.save(feedback);
        return feedbackMapper.toDto(feedback);
    }

    @Transactional
    @Override
    public FeedbackDeleteResponse deleteFeedback(List<String> feedbackIds) {

        List<Feedback> feedbacks = feedbackRepository.findByIds(feedbackIds);
        AtomicInteger deleteCount = new AtomicInteger();
        feedbacks.stream().map(feedback -> {
            deleteCount.getAndIncrement();
            feedback.setActive(false);
            return feedback;
        }).toList();
        return FeedbackDeleteResponse.builder()
                .deleteCount(feedbackIds.size())
                .success(deleteCount.get())
                .message("Deleted " + deleteCount.get() + " out of " + feedbackIds.size() + " feedback(s)")
                .build();
    }

    private void calculateStar(FeedbackResponse feedback) {
        if (feedback.getFeedbackType().equals(FeedbackType.SHOP_FEEDBACK)) {

            kafkaTemplate.send("shop-feedback", feedback.getShopId().toString(), feedback);
        } else if (feedback.getFeedbackType().equals(FeedbackType.PRODUCT_FEEDBACK)) {
            kafkaTemplate.send("product-feedback", feedback.getProductId().toString(), feedback);
        }
    }
}
