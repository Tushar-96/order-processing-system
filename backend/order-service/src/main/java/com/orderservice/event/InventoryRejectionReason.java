package com.orderservice.event;

public enum InventoryRejectionReason {
    PRODUCT_NOT_FOUND,
    INSUFFICIENT_STOCK,
    INVALID_ORDER,
    PROCESSING_ERROR
}
