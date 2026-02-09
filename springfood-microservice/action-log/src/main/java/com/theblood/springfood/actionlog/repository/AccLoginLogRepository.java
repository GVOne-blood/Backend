package com.theblood.springfood.actionlog.repository;

import com.theblood.springfood.actionlog.domain.AccLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AccLoginLog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccLoginLogRepository extends JpaRepository<AccLoginLog, String>, JpaSpecificationExecutor<AccLoginLog> {
}
