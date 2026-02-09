package com.theblood.springfood.actionlog.service.mapper;

import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
import com.theblood.springfood.actionlog.domain.enumeration.ActionTypeAnnualUpdate;
import com.theblood.springfood.actionlog.service.dto.LogActionAnnualUpdateDTO;
import com.theblood.springfood.client.api.LogActionAnnualUpdateClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link LogActionAnnualUpdate} and its DTO {@link LogActionAnnualUpdateDTO}.
 */
@Mapper(componentModel = "spring")
public interface LogActionAnnualUpdateMapper extends EntityMapper<LogActionAnnualUpdateDTO, LogActionAnnualUpdate> {

    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "stringToActionType")
    LogActionAnnualUpdate toEntity(LogActionAnnualUpdateClient.LogActionAnnualUpdateDto dto);

    @Named("stringToActionType")
    default ActionTypeAnnualUpdate stringToActionType(String actionType) {
        if (actionType == null) return null;
        try {
            return ActionTypeAnnualUpdate.valueOf(actionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
