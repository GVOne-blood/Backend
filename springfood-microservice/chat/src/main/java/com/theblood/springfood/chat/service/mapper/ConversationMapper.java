package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationSettings;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.ConversationSettingsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Conversation} and its DTO {@link ConversationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationMapper extends EntityMapper<ConversationDTO, Conversation> {
    @Mapping(target = "settings", source = "settings", qualifiedByName = "conversationSettingsSettingsId")
    ConversationDTO toDto(Conversation s);

    @Named("conversationSettingsSettingsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "settingsId", source = "settingsId")
    ConversationSettingsDTO toDtoConversationSettingsSettingsId(ConversationSettings conversationSettings);
}
