package com.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "application.security.jwt.secret-key=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
    "application.security.jwt.expiration-ms=86400000"
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
