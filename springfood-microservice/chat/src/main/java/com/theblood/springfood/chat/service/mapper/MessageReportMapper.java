package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.MessageReport;
import com.theblood.springfood.chat.service.dto.MessageReportDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageReport} and its DTO {@link MessageReportDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageReportMapper extends EntityMapper<MessageReportDTO, MessageReport> {}
