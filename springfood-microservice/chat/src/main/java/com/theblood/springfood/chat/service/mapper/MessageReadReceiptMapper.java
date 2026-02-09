package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.dto.MessageReadReceiptDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageReadReceipt} and its DTO {@link MessageReadReceiptDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageReadReceiptMapper extends EntityMapper<MessageReadReceiptDTO, MessageReadReceipt> {
    @Mapping(target = "message", source = "message", qualifiedByName = "messageMessageId")
    MessageReadReceiptDTO toDto(MessageReadReceipt s);

    @Named("messageMessageId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "messageId", source = "messageId")
    MessageDTO toDtoMessageMessageId(Message message);
}
