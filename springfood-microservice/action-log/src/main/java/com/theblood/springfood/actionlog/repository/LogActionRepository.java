package com.theblood.springfood.actionlog.repository;

import com.theblood.springfood.actionlog.domain.LogAction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the LogAction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LogActionRepository extends JpaRepository<LogAction, String>, JpaSpecificationExecutor<LogAction> {

    /**
     * Find all log actions by table name and object ID with sorting.
     *
     * @param tableName the table name to search for
     * @param objectId  the object ID to search for
     * @param sort      the sorting information
     * @return a list of all log actions matching the criteria
     */
    List<LogAction> findByTableNameAndObjectIdContains(String tableName, String objectId, Sort sort);
}
