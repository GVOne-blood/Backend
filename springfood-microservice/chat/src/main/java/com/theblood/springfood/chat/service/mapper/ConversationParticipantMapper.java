package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.service.dto.ConversationParticipantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ConversationParticipant} and its DTO {@link ConversationParticipantDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationParticipantMapper extends EntityMapper<ConversationParticipantDTO, ConversationParticipant> {
    @Mapping(target = "conversationId", source = "conversation.conversationId")
    ConversationParticipantDTO toDto(ConversationParticipant s);

    @Mapping(target = "conversation", ignore = true)
    ConversationParticipant toEntity(ConversationParticipantDTO dto);
}
