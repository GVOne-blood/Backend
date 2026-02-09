package com.theblood.springfood.actionlog.repository;

import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the LogActionAnnualUpdate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LogActionAnnualUpdateRepository
        extends JpaRepository<LogActionAnnualUpdate, String>, JpaSpecificationExecutor<LogActionAnnualUpdate> {

    /**
     * Find all log actions by table name and object ID with sorting.
     *
     * @param tableName the table name to search for
     * @param objectId  the object ID to search for
     * @param sort      the sorting information
     * @return a list of all log actions matching the criteria
     */
    List<LogActionAnnualUpdate> findByTableNameAndObjectId(String tableName, String objectId, Sort sort);
}
