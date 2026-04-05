package com.theblood.productservice.service.mapper;

import com.theblood.productservice.domain.Feedback;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    FeedbackResponse toDto(Feedback feedback);

    Feedback toEntity(FeedbackRequest feedbackResponse);
}
