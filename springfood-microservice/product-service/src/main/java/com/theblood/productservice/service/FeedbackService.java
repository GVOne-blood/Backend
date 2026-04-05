package com.theblood.productservice.service;

import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackDeleteResponse;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackService {

    Page<FeedbackResponse> getShopFeedback(Pageable pageable, String userId);

    Page<FeedbackResponse> getProductFeedback(Pageable pageable, String productId);

    FeedbackResponse createFeedback(FeedbackRequest feedbackRequest);

    FeedbackResponse updateFeedback(FeedbackRequest feedbackRequest);

    FeedbackDeleteResponse deleteFeedback(List<String> feedbackRequest);
}
