package com.theblood.springfood.actionlog.service;

import com.theblood.springfood.actionlog.carbone.CarboneService;
import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate_;
import com.theblood.springfood.actionlog.repository.LogActionAnnualUpdateRepository;
import com.theblood.springfood.actionlog.service.criteria.LogActionAnnualUpdateCriteria;
import com.theblood.springfood.actionlog.service.dto.LogActionAnnualUpdateDTO;
import com.theblood.springfood.actionlog.service.dto.LogActionRequest;
import com.theblood.springfood.actionlog.service.mapper.LogActionAnnualUpdateMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

import java.util.List;

/**
 * Service for executing complex queries for {@link LogActionAnnualUpdate} entities in the database.
 * The main input is a {@link LogActionAnnualUpdateCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link LogActionAnnualUpdateDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LogActionAnnualUpdateQueryService extends QueryService<LogActionAnnualUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LogActionAnnualUpdateQueryService.class);

    private final CarboneService carboneService;

    private final LogActionAnnualUpdateRepository logActionAnnualUpdateRepository;

    private final LogActionAnnualUpdateMapper logActionAnnualUpdateMapper;

    /**
     * Return a {@link Page} of {@link LogActionAnnualUpdateDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<LogActionAnnualUpdateDTO> findByCriteria(LogActionAnnualUpdateCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<LogActionAnnualUpdate> specification = createSpecification(criteria);
        return logActionAnnualUpdateRepository.findAll(specification, page).map(logActionAnnualUpdateMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(LogActionAnnualUpdateCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<LogActionAnnualUpdate> specification = createSpecification(criteria);
        return logActionAnnualUpdateRepository.count(specification);
    }

    /**
     * Find all log actions by table name and object ID without pagination.
     *
     * @param logActionRequest the request containing tableName and objectId
     * @return a list of all log actions matching the criteria
     */
    @Transactional(readOnly = true)
    public List<LogActionAnnualUpdateDTO> findByTableNameAndObjectId(LogActionRequest logActionRequest) {
        LOG.debug("find by tableName: {} and objectId: {}",
                logActionRequest.getTableName(),
                logActionRequest.getObjectId());

        org.springframework.data.domain.Sort sort = createSort(logActionRequest);

        List<LogActionAnnualUpdate> logActions = logActionAnnualUpdateRepository.findByTableNameAndObjectId(
                logActionRequest.getTableName(),
                logActionRequest.getObjectId(),
                sort
        );

        return logActions.stream()
                .map(logActionAnnualUpdateMapper::toDto).toList();
    }

    /**
     * Create Sort from LogActionRequest.
     */
    private org.springframework.data.domain.Sort createSort(LogActionRequest request) {
        if (request.getSort() != null && !request.getSort().isEmpty()) {
            String[] sortParts = request.getSort().split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim();
                org.springframework.data.domain.Sort.Direction sortDirection =
                        "desc".equalsIgnoreCase(direction)
                                ? org.springframework.data.domain.Sort.Direction.DESC
                                : org.springframework.data.domain.Sort.Direction.ASC;
                return org.springframework.data.domain.Sort.by(sortDirection, field);
            }
        }

        // Default sort by createdDate descending
        return org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC,
                "createdDate"
        );
    }

    /**
     * Function to convert {@link LogActionAnnualUpdateCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<LogActionAnnualUpdate> createSpecification(LogActionAnnualUpdateCriteria criteria) {
        Specification<LogActionAnnualUpdate> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getId(), LogActionAnnualUpdate_.id));
            }
            if (criteria.getAccountId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountId(), LogActionAnnualUpdate_.accountId));
            }
            if (criteria.getUserName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUserName(), LogActionAnnualUpdate_.userName));
            }
            if (criteria.getOrganizationId() != null) {
                specification = specification.and(
                        buildStringSpecification(criteria.getOrganizationId(), LogActionAnnualUpdate_.organizationId));
            }
            if (criteria.getActionType() != null) {
                specification = specification.and(buildSpecification(criteria.getActionType(), LogActionAnnualUpdate_.actionType));
            }
            if (criteria.getIpAddress() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIpAddress(), LogActionAnnualUpdate_.ipAddress));
            }
            if (criteria.getObjectId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getObjectId(), LogActionAnnualUpdate_.objectId));
            }
            if (criteria.getAffectCurrent() != null) {
                specification = specification.and(
                        buildRangeSpecification(criteria.getAffectCurrent(), LogActionAnnualUpdate_.affectCurrent)
                );
            }
        }
        return specification;
    }
}
