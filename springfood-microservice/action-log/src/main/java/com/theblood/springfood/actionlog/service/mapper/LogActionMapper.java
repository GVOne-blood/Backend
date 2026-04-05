package com.theblood.springfood.actionlog.service.mapper;

import com.theblood.springfood.actionlog.domain.LogAction;
import com.theblood.springfood.actionlog.service.dto.LogActionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link LogAction} and its DTO {@link LogActionDTO}.
 */
@Mapper(componentModel = "spring")
public interface LogActionMapper extends EntityMapper<LogActionDTO, LogAction> {

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "lastModifiedBy", source = "lastModifiedBy")
    @Mapping(target = "lastModifiedDate", source = "lastModifiedDate")
    LogActionDTO toDto(LogAction entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    LogAction toEntity(LogActionDTO dto);
}
