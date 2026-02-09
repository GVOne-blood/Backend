package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.ConversationParticipantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ConversationParticipant} and its DTO {@link ConversationParticipantDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationParticipantMapper extends EntityMapper<ConversationParticipantDTO, ConversationParticipant> {
    @Mapping(target = "conversation", source = "conversation", qualifiedByName = "conversationConversationId")
    ConversationParticipantDTO toDto(ConversationParticipant s);

    @Named("conversationConversationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "conversationId", source = "conversationId")
    ConversationDTO toDtoConversationConversationId(Conversation conversation);
}
