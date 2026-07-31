package com.ecommerce.inventory.service;

import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventory.dto.CreateProductRequest;
import com.ecommerce.inventory.dto.ProductResponse;
import com.ecommerce.inventory.dto.UpdateProductRequest;
import com.ecommerce.inventory.exception.ProductNotFoundException;
import com.ecommerce.inventory.model.Product;
import com.ecommerce.inventory.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long productId) {
        Product product = findProduct(productId);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {

        Product product = Product.builder()
                .name(request.name().trim())
                .description(normalizeDescription(request.description()))
                .price(request.price().setScale(2, RoundingMode.HALF_UP))
                .availableQuantity(request.availableQuantity())
                .build();

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(
            Long productId,
            UpdateProductRequest request) {

        Product product = findProduct(productId);

        product.setName(request.name().trim());
        product.setDescription(
                normalizeDescription(request.description())
        );
        product.setPrice(
                request.price().setScale(2, RoundingMode.HALF_UP)
        );
        product.setAvailableQuantity(request.availableQuantity());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    @Transactional
    public void delete(Long productId) {

        Product product = findProduct(productId);

        productRepository.delete(product);
    }

    private Product findProduct(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getAvailableQuantity(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}