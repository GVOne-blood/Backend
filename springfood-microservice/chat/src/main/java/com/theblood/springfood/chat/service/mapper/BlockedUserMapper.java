package com.theblood.springfood.chat.service.mapper;

import com.theblood.springfood.chat.domain.BlockedUser;
import com.theblood.springfood.chat.service.dto.BlockedUserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BlockedUser} and its DTO {@link BlockedUserDTO}.
 */
@Mapper(componentModel = "spring")
public interface BlockedUserMapper extends EntityMapper<BlockedUserDTO, BlockedUser> {}
