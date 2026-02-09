package com.theblood.springfood.client.service;

import com.theblood.springfood.client.api.ClientRequest;
import com.theblood.springfood.client.api.LogActionAnnualUpdateClient;
import com.theblood.springfood.client.api.LogActionAnnualUpdateClient.LogActionAnnualUpdateDto;
import com.theblood.springfood.client.api.LogActionsClient;
import com.theblood.springfood.client.api.LogActionsClient.LogActionsDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoggingService {

    private final LogActionsClient logActionsClient;
    private final LogActionAnnualUpdateClient logActionAnnualUpdateClient;
    Logger logger = LoggerFactory.getLogger(LoggingService.class);

    /**
     * Create a new log action.
     *
     * @param actionType  the type of action (CREATED, UPDATED, DELETED, etc.)
     * @param oldValue    the old value before the action
     * @param newValue    the new value after the action
     * @param description description of the action
     * @param tableName   the table name where the action occurred
     * @param objectId    the ID of the object that was affected
     * @param accountId   the account ID of the user who performed the action
     * @param userName    the username of the user who performed the action
     * @param shopId      the organization ID
     * @param ipAddress   the IP address of the user
     * @param userAgent   the user agent (browser/client info)
     * @return true if the log was created successfully, false otherwise
     */
    public Boolean createLogAction(String actionType, String oldValue,
                                   String newValue, String description,
                                   String tableName, String objectId,
                                   String accountId, String userName,
                                   String shopId, String ipAddress,
                                   String userAgent) {

        LogActionsDto log = LogActionsDto.builder().actionType(actionType)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .tableName(tableName)
                .objectId(objectId)
                .accountId(accountId)
                .userName(userName)
                .shopId(shopId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        ClientRequest<LogActionsDto> request = ClientRequest.<LogActionsDto>builder()
                .body(log)
                .build();

        Boolean body = null;
        try {
            var response = logActionsClient.createLogAction(request);

            if (response != null) {
                body = response.getBody();
                if (Boolean.TRUE.equals(body)) {
                    logger.info("Successfully created LogAction: actionType='{}', table='{}', objectId='{}', accountId='{}', userName='{}'",
                            actionType, tableName, objectId, accountId, userName);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Exception while creating LogAction", e);
        }

        logger.warn("Failed to create LogAction: actionType='{}', table='{}', objectId='{}', accountId='{}', userName='{}'. Response body={}",
                actionType, tableName, objectId, accountId, userName, body);

        return false;
    }

    /**
     * Find all log actions by table name and object ID.
     *
     * @param tableName the table name to search for
     * @param objectId  the object ID to search for
     * @return list of log actions matching the criteria
     */
    public java.util.List<LogActionsDto> findByTableNameAndObjectId(String tableName, String objectId) {
        return findByTableNameAndObjectId(tableName, objectId, "createdDate,desc");
    }

    /**
     * Find all log actions by table name and object ID with custom sorting.
     *
     * @param tableName the table name to search for
     * @param objectId  the object ID to search for
     * @param sort      the sort field and direction (e.g., "createdDate,desc")
     * @return list of log actions matching the criteria
     */
    public java.util.List<LogActionsDto> findByTableNameAndObjectId(String tableName, String objectId, String sort) {
        logger.debug("Searching LogActions: tableName='{}', objectId='{}', sort='{}'", tableName, objectId, sort);

        LogActionsClient.LogActionSearchRequest searchRequest = LogActionsClient.LogActionSearchRequest.builder()
                .tableName(tableName)
                .objectId(objectId)
                .sort(sort)
                .build();

        ClientRequest<LogActionsClient.LogActionSearchRequest> request =
                ClientRequest.<LogActionsClient.LogActionSearchRequest>builder()
                        .body(searchRequest)
                        .build();

        try {
            var response = logActionsClient.findByTableNameAndObjectId(request);
            if (response != null && response.getBody() != null) {
                logger.info("Successfully retrieved {} LogActions for tableName='{}', objectId='{}'",
                        response.getBody().size(), tableName, objectId);
                return response.getBody();
            } else {
                logger.warn("No LogActions found for tableName='{}', objectId='{}'", tableName, objectId);
                return java.util.Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve LogActions for tableName='{}', objectId='{}'", tableName, objectId, e);
            return java.util.Collections.emptyList();
        }
    }

    public java.util.List<LogActionAnnualUpdateDto> findLogAnnualUpdateByTableNameAndObjectId(String tableName, String objectId, String sort) {
        logger.debug("Searching LogActions: tableName='{}', objectId='{}', sort='{}'", tableName, objectId, sort);

        LogActionAnnualUpdateClient.LogActionSearchRequest searchRequest = LogActionAnnualUpdateClient.LogActionSearchRequest.builder()
                .tableName(tableName)
                .objectId(objectId)
                .sort(sort)
                .build();

        ClientRequest<LogActionAnnualUpdateClient.LogActionSearchRequest> request =
                ClientRequest.<LogActionAnnualUpdateClient.LogActionSearchRequest>builder()
                        .body(searchRequest)
                        .build();

        try {
            var response = logActionAnnualUpdateClient.findByTableNameAndObjectId(request);
            if (response != null && response.getBody() != null) {
                logger.info("Successfully retrieved {} LogActions for tableName='{}', objectId='{}'",
                        response.getBody().size(), tableName, objectId);
                return response.getBody();
            } else {
                logger.warn("No LogActions found for tableName='{}', objectId='{}'", tableName, objectId);
                return java.util.Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve LogActions for tableName='{}', objectId='{}'", tableName, objectId, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Create a new log action.
     *
     * @param actionType  the type of action (CREATED, UPDATED, DELETED, etc.)
     * @param oldValue    the old value before the action
     * @param newValue    the new value after the action
     * @param description description of the action
     * @param tableName   the table name where the action occurred
     * @param objectId    the ID of the object that was affected
     * @param accountId   the account ID of the user who performed the action
     * @param userName    the username of the user who performed the action
     * @param shopId      the organization ID
     * @param ipAddress   the IP address of the user
     * @param userAgent   the user agent (browser/client info)
     * @return true if the log was created successfully, false otherwise
     */
    public Boolean createLogActionAnnualUpdate(String actionType, String oldValue,
                                               String newValue, String description,
                                               String tableName, String objectId,
                                               String accountId, String userName,
                                               String shopId, String ipAddress,
                                               String userAgent) {

        LogActionAnnualUpdateDto log = LogActionAnnualUpdateDto.builder().actionType(actionType)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .tableName(tableName)
                .objectId(objectId)
                .accountId(accountId)
                .userName(userName)
                .shopId(shopId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        ClientRequest<LogActionAnnualUpdateDto> request = ClientRequest.<LogActionAnnualUpdateDto>builder()
                .body(log)
                .build();


        Boolean body = null;
        try {
            var response = logActionAnnualUpdateClient.createLogActionAnnualUpdate(request);

            if (response != null) {
                body = response.getBody();
                if (Boolean.TRUE.equals(body)) {
                    logger.info("Successfully created LogAction: actionType='{}', table='{}', objectId='{}', accountId='{}', userName='{}'",
                            actionType, tableName, objectId, accountId, userName);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Exception while creating LogAction", e);
        }

        logger.warn("Failed to create LogAction: actionType='{}', table='{}', objectId='{}', accountId='{}', userName='{}'. Response body={}",
                actionType, tableName, objectId, accountId, userName, body);

        return false;
    }

}
