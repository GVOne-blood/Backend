package com.spring_food.springfood.repository.impl;

import com.spring_food.springfood.model.Product;
import com.spring_food.springfood.repository.CustomProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductRepositoryImpl implements CustomProductRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<Product> findByPrice(BigDecimal from, BigDecimal to, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        Predicate condition = cb.between(root.get("price"), from, to);
        cq.where(condition);

        if (pageable.getSort().isSorted()){
            cq.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));
        }


        TypedQuery<Product> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset()); // OFFSET
        query.setMaxResults(pageable.getPageSize()); // LIMIT
        List<Product> products = query.getResultList();


        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<Product> rootCount = cqCount.from(Product.class);
        Predicate conditionCount = cb.between(rootCount.get("price"), from, to);
        cqCount.select(cb.count(rootCount)).where(conditionCount);

        long totalElements = entityManager.createQuery(cqCount).getSingleResult();


        return new PageImpl<>(products, pageable, totalElements);
    }
}
