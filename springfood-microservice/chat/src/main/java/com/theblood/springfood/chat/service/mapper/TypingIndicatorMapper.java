package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.TypingIndicator;
import com.theblood.springfood.chat.service.dto.TypingIndicatorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TypingIndicator} and its DTO {@link TypingIndicatorDTO}.
 */
@Mapper(componentModel = "spring")
public interface TypingIndicatorMapper extends EntityMapper<TypingIndicatorDTO, TypingIndicator> {}
