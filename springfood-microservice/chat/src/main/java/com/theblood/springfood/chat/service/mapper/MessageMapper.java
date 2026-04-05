package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 * Also handles mapping to/from Kafka events.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    @Mapping(target = "conversationId", source = "conversation.conversationId")
    @Mapping(target = "createdAt", source = "createdDate")
    MessageDTO toDto(Message s);

    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "readReceipts", ignore = true)
    @Mapping(target = "createdDate", source = "createdAt")
    Message toEntity(MessageDTO dto);

    /**
     * Map Message entity to ChatMessageEvent for Kafka publishing.
     */
    @Mapping(target = "conversationId", source = "conversation.conversationId")
    @Mapping(target = "createdAt", source = "createdDate")
    ChatMessageEvent toEvent(Message message);

    /**
     * Map ChatMessageEvent to Message entity for persistence.
     */
    @Mapping(target = "messageId", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "readReceipts", ignore = true)
    @Mapping(target = "createdDate", source = "createdAt")
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    Message eventToEntity(ChatMessageEvent event);
}
