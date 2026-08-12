package com.ecommerce.inventory.event;

import java.math.BigDecimal;

public record ReservedInventoryItem(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
        ) {

}
