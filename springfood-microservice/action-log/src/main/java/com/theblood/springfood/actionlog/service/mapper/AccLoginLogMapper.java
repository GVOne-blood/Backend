package com.theblood.springfood.actionlog.service.mapper;

import com.theblood.springfood.actionlog.domain.AccLoginLog;
import com.theblood.springfood.actionlog.service.dto.AccLoginLogDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link AccLoginLog} and its DTO {@link AccLoginLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AccLoginLogMapper extends EntityMapper<AccLoginLogDTO, AccLoginLog> {
}
