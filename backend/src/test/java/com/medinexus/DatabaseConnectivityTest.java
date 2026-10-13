package com.medinexus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class DatabaseConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void verifyMySqlConnectivityAndSafety() throws Exception {
        assertNotNull(dataSource, "DataSource bean should not be null");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "MySQL connection must be established");
            assertFalse(connection.isClosed(), "Connection must be active");

            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();

            System.out.println("Verified Database: " + productName + " v" + productVersion);
            assertTrue(productName.toLowerCase().contains("mysql"),
                    "Database product must be MySQL, found: " + productName);

            // Execute verification ping query
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT 1")) {
                assertTrue(rs.next(), "Ping query must return a row");
                assertEquals(1, rs.getInt(1), "Ping query must return 1");
            }

            // Verify current schema catalog
            String catalog = connection.getCatalog();
            assertEquals("student_db", catalog, "Connected catalog must be student_db");
        }
    }
}
