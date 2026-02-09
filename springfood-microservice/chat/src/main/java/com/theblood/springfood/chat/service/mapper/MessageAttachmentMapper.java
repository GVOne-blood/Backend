package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageAttachment;
import com.theblood.springfood.chat.service.dto.MessageAttachmentDTO;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageAttachment} and its DTO {@link MessageAttachmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageAttachmentMapper extends EntityMapper<MessageAttachmentDTO, MessageAttachment> {
    @Mapping(target = "message", source = "message", qualifiedByName = "messageMessageId")
    MessageAttachmentDTO toDto(MessageAttachment s);

    @Named("messageMessageId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "messageId", source = "messageId")
    MessageDTO toDtoMessageMessageId(Message message);
}
