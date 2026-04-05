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
    @Mapping(target = "conversationId", source = "conversationId")
    @Mapping(target = "lastMessageId", source = "lastMessageId")
    @Mapping(target = "createdAt", source = "createdDate")
    ConversationDTO toDto(Conversation s);

    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "createdDate", source = "createdAt")
    Conversation toEntity(ConversationDTO dto);

    @Named("conversationSettingsSettingsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "settingsId", source = "settingsId")
    ConversationSettingsDTO toDtoConversationSettingsSettingsId(ConversationSettings conversationSettings);
}
