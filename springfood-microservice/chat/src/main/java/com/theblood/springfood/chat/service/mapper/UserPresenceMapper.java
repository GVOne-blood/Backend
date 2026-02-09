package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.UserPresence;
import com.theblood.springfood.chat.service.dto.UserPresenceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link UserPresence} and its DTO {@link UserPresenceDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserPresenceMapper extends EntityMapper<UserPresenceDTO, UserPresence> {}
