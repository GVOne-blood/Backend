package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    @Mapping(target = "conversation", source = "conversation", qualifiedByName = "conversationConversationId")
    MessageDTO toDto(Message s);

    @Named("conversationConversationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "conversationId", source = "conversationId")
    ConversationDTO toDtoConversationConversationId(Conversation conversation);
}
