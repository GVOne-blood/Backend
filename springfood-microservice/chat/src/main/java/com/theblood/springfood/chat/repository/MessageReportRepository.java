package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.MessageReport;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MessageReport entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageReportRepository extends JpaRepository<MessageReport, String> {}
