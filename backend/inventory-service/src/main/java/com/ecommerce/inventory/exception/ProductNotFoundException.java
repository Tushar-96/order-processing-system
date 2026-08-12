package com.ecommerce.inventory.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " was not found");
    }
}