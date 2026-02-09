package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.ConversationSettings;
import com.theblood.springfood.chat.service.dto.ConversationSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ConversationSettings} and its DTO {@link ConversationSettingsDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationSettingsMapper extends EntityMapper<ConversationSettingsDTO, ConversationSettings> {}
