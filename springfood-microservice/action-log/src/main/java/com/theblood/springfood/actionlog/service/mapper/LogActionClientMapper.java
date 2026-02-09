package com.theblood.springfood.actionlog.service.mapper;

import com.theblood.springfood.actionlog.domain.LogAction;
import com.theblood.springfood.actionlog.domain.enumeration.ActionType;
import com.theblood.springfood.client.api.LogActionsClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LogActionClientMapper extends EntityMapper<LogActionsClient.LogActionsDto, LogAction> {

    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "stringToActionType")
    LogAction toEntity(LogActionsClient.LogActionsDto dto);

    @Named("stringToActionType")
    default ActionType stringToActionType(String actionType) {
        if (actionType == null) return null;
        try {
            return ActionType.valueOf(actionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
