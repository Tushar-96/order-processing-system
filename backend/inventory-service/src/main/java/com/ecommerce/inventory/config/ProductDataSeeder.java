package com.ecommerce.inventory.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventory.model.Product;
import com.ecommerce.inventory.repository.ProductRepository;

@Component
@ConditionalOnProperty(
        name = "application.seed.products-enabled",
        havingValue = "true"
)
public class ProductDataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;

    public ProductDataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                createProduct(
                        "Mechanical Keyboard",
                        "RGB mechanical keyboard with brown switches",
                        "3499.00",
                        25
                ),
                createProduct(
                        "Wireless Mouse",
                        "Ergonomic wireless mouse with adjustable DPI",
                        "1499.00",
                        40
                ),
                createProduct(
                        "USB-C Hub",
                        "Seven-port USB-C hub with HDMI and Ethernet",
                        "2499.00",
                        20
                ),
                createProduct(
                        "27-inch Monitor",
                        "QHD IPS monitor with 144 Hz refresh rate",
                        "24999.00",
                        10
                ),
                createProduct(
                        "Laptop Stand",
                        "Adjustable aluminium laptop stand",
                        "1999.00",
                        30
                )
        );

        productRepository.saveAll(products);
    }

    private Product createProduct(
            String name,
            String description,
            String price,
            int availableQuantity) {

        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .availableQuantity(availableQuantity)
                .build();
    }
}
