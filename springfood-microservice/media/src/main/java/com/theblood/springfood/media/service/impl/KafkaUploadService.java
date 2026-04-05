package com.theblood.springfood.media.service.impl;

import com.theblood.springfood.media.service.dto.FileResponse;
import com.theblood.springfood.media.service.dto.MultiFileResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaUploadService {

    KafkaTemplate<String, Object> kafkaTemplate;
    RetryTemplate retryTemplate;
    String uploadTopic = "media-upload-events";
    long MAX_SEND_TIMEOUT = 5;

    public void mergeUserAvatar(FileResponse fileResponse) throws Exception {

        retryTemplate.execute(

            context -> {
                CompletableFuture<SendResult<String, Object>> future
                    = kafkaTemplate.send(uploadTopic, fileResponse);

                try {
                    SendResult<String, Object> result = future.get(MAX_SEND_TIMEOUT, TimeUnit.SECONDS);
                    log.info("Send OK: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                    return result;
                } catch (TimeoutException e) {
                    log.warn("Send timeout after {}s for key={}", MAX_SEND_TIMEOUT, uploadTopic);

                    throw e;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    log.warn("Send failed for key={}, cause={}", uploadTopic, cause.toString());
                    throw new RuntimeException("Kafka send failed", cause); // throw để retry
                }
            }
        );

    }

    // TODO: Configure Kafka listener topic
    // @KafkaListener(topics = "media-product-images")
    public void mergeProductImage(MultiFileResponse multiFileResponse) {
        kafkaTemplate.send(uploadTopic, multiFileResponse);
    }
}
