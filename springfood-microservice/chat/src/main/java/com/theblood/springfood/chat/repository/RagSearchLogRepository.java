package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.RagSearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for RAG search logs (analytics)
 */
@Repository
public interface RagSearchLogRepository extends JpaRepository<RagSearchLog, Long> {
    
}
