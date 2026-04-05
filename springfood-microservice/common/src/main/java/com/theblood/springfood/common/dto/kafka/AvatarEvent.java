package com.theblood.springfood.common.dto.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvatarEvent {
    String obejctId;
    String avatarUrl;
    
}
