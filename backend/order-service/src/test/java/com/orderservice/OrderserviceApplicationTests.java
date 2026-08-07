package com.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "application.security.jwt.secret-key="
    + "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
class OrderserviceApplicationTests {

    @Test
    void contextLoads() {
    }

}
