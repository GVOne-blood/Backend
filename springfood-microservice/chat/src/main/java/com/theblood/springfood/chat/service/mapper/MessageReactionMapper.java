package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.service.dto.MessageReactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageReaction} and its DTO {@link MessageReactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageReactionMapper extends EntityMapper<MessageReactionDTO, MessageReaction> {
    @Mapping(target = "messageId", source = "message.messageId")
    MessageReactionDTO toDto(MessageReaction s);

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    MessageReaction toEntity(MessageReactionDTO dto);
}
