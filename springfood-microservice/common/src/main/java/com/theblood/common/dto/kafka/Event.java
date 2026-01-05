package com.theblood.common.dto.kafka;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    String eventType;
    String transactionId;
    Object payload;
    String reason;
}
