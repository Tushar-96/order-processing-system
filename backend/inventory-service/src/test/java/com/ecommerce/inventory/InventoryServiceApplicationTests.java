package com.ecommerce.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "application.seed.products-enabled=false"
})
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
