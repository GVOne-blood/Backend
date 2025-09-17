package com.spring_food.springfood.specification;

import com.spring_food.springfood.dto.request.SearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface SearchSpecification {


    /** String format
     * Overload methods
     * @param field
     * @param key
     * @return
     * @param <T> - entity
     */
    public static <T> Specification<T> contain(String field, String key){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get(field), "%" + key + "%");
    }


    public static <T> Specification<T> equals(String field, String key){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get(field), key);
    }

    public static <T> Specification<T> notEquals(String field, String key){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.notEqual(root.get(field), key);
    }

    public static <T> Specification<T> isNull(String field){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.isNull(root.get(field));
    }

    /** Number format
     * Overload methods
     * @param field
     * @param value
     * @return
     * @param <T> - entity
     */
    public static <T> Specification<T> lessThan(String field, Integer value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThan(root.get(field), value);
    }
    public static <T> Specification<T> lessThan(String field, Double value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThan(root.get(field), value);
    }

    public static <T> Specification<T> lessThanOrEqual(String field, Integer value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
    }
    public static <T> Specification<T> lessThanOrEqual(String field, Double value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> greaterThan(String field, Integer value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThan(root.get(field), value);
    }
    public static <T> Specification<T> greaterThan(String field, Double value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThan(root.get(field), value);
    }

    public static <T> Specification<T> greaterThanOrEqual(String field, Integer value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
    }
    public static <T> Specification<T> greaterThanOrEqual(String field, Double value){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> between(String field, BigDecimal from, BigDecimal to){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(root.get(field), from, to);
    }
    public static <T> Specification<T> between(String field, Integer from, Integer to){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(root.get(field), from, to);
    }

    /** process operation -> condition
     *
     * @param searchParam
     * @return
     * @param <T> with Predicate
     */

    public static <T> Specification<T> buildSpecification(SearchCriteria searchParam) {
        return (root, query, criteriaBuilder) -> {
            String keyword = searchParam.getKeyword();
            String operation = searchParam.getOperation();
            String value = searchParam.getValue();

            if (value == null || value.isBlank()) {
                return null;
            }

            try {
                Path<Object> fieldPath = extractField(root, keyword);

                switch (operation) {
                    case "=":
                        if (isNumeric(value)) {
                            return criteriaBuilder.equal(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.equal(fieldPath.as(String.class), value);

                    case "!=":
                        if (isNumeric(value)) {
                            return criteriaBuilder.notEqual(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.notEqual(fieldPath.as(String.class), value);

                    case ">":
                        if (isNumeric(value)) {
                            return criteriaBuilder.greaterThan(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.greaterThan(fieldPath.as(String.class), value);

                    case ">=":
                        if (isNumeric(value)) {
                            return criteriaBuilder.greaterThanOrEqualTo(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.greaterThanOrEqualTo(fieldPath.as(String.class), value);

                    case "<":
                        if (isNumeric(value)) {
                            return criteriaBuilder.lessThan(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.lessThan(fieldPath.as(String.class), value);

                    case "<=":
                        if (isNumeric(value)) {
                            return criteriaBuilder.lessThanOrEqualTo(fieldPath.as(Double.class), Double.parseDouble(value));
                        }
                        return criteriaBuilder.lessThanOrEqualTo(fieldPath.as(String.class), value);

                    case ":": // LIKE operation
                        return criteriaBuilder.like(criteriaBuilder.lower(fieldPath.as(String.class)),
                                "%" + value.toLowerCase() + "%");

                    case "~": // BETWEEN operation
                        String[] range = value.split("-");
                        if (range.length == 2 && isNumeric(range[0]) && isNumeric(range[1])) {
                            Double start = Double.parseDouble(range[0].trim());
                            Double end = Double.parseDouble(range[1].trim());
                            return criteriaBuilder.between(fieldPath.as(Double.class), start, end);
                        }
                        return null;

                    default:
                        return null;
                }
            } catch (Exception e) {
                return null;
            }
        };
    }

    /**
     * Lấy Path cho một thuộc tính, xử lý join nếu cần.
     * Ví dụ: "category.name" sẽ join bảng 'category' và trả về Path của trường 'name'.
     */
    private static <T> Path<Object> extractField(Root<T> root, String keyword) {
        if (!keyword.contains(".")) {
            return root.get(keyword);
        }

        String[] keys = keyword.split("\\.");

        Join<?, ?> currentJoin = root.join(keys[0]);
        for (int i = 1; i < keys.length - 1; i++) {
            currentJoin = currentJoin.join(keys[i]);
        }

        return currentJoin.get(keys[keys.length - 1]);
    }

    /**
     * Kiểm tra một chuỗi có phải là số hay không.
     */
    private static boolean isNumeric(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
