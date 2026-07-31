package com.ecommerce.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.inventory.model.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select product
            from Product product
            where product.id = :productId
            """)
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId
    );
}
