package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.dto.MessageReactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageReaction} and its DTO {@link MessageReactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageReactionMapper extends EntityMapper<MessageReactionDTO, MessageReaction> {
    @Mapping(target = "message", source = "message", qualifiedByName = "messageMessageId")
    MessageReactionDTO toDto(MessageReaction s);

    @Named("messageMessageId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "messageId", source = "messageId")
    MessageDTO toDtoMessageMessageId(Message message);
}
