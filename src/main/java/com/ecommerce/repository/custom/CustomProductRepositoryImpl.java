package com.ecommerce.repository.custom;

import com.ecommerce.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findByAdvancedFilters(String keyword, Long categoryId,
                                              Double minPrice, Double maxPrice,
                                              Integer minRating, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            predicates.add(cb.or(
                cb.like(cb.lower(product.get("name")), "%" + keyword.toLowerCase() + "%"),
                cb.like(cb.lower(product.get("description")), "%" + keyword.toLowerCase() + "%")
            ));
        }

        if (categoryId != null) {
            predicates.add(cb.equal(product.get("category").get("id"), categoryId));
        }

        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), maxPrice));
        }

        if (minRating != null) {
            Subquery<Double> ratingSubquery = query.subquery(Double.class);
            Root<Product> ratingRoot = ratingSubquery.from(Product.class);
            ratingSubquery.select(cb.avg(ratingRoot.join("reviews").get("rating")))
                .where(cb.equal(ratingRoot.get("id"), product.get("id")));
            predicates.add(cb.greaterThanOrEqualTo(ratingSubquery, minRating.doubleValue()));
        }

        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply ordering from pageable
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(sort -> {
                if (sort.isAscending()) {
                    orders.add(cb.asc(product.get(sort.getProperty())));
                } else {
                    orders.add(cb.desc(product.get(sort.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        List<Product> resultList = entityManager.createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        // Get total count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public List<Product> findRelatedProducts(Long productId, int limit) {
        // Find products in the same category
        Product currentProduct = entityManager.find(Product.class, productId);
        if (currentProduct == null) {
            return new ArrayList<>();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);
        
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(product.get("category").get("id"), 
                               currentProduct.getCategory().getId()));
        predicates.add(cb.notEqual(product.get("id"), productId));
        
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        
        return entityManager.createQuery(query)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public List<Product> findFrequentlyBoughtTogether(Long productId, int limit) {
        // This is a more complex query using native SQL 
        // to find products frequently bought together
        String sql = "SELECT p.* FROM product p " +
                    "JOIN order_item oi1 ON p.id = oi1.product_id " +
                    "JOIN order_item oi2 ON oi1.order_id = oi2.order_id " +
                    "WHERE oi2.product_id = :productId AND p.id != :productId " +
                    "GROUP BY p.id " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT :limit";
                    
        return entityManager.createNativeQuery(sql, Product.class)
            .setParameter("productId", productId)
            .setParameter("limit", limit)
            .getResultList();
    }
}
