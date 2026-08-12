package com.ecommerce.inventory.integration;

import java.sql.Connection;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostgresConnectionTest extends PostgresIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldConnectToPostgresContainer() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName())
                    .isEqualTo("PostgreSQL");
        }
    }
}
