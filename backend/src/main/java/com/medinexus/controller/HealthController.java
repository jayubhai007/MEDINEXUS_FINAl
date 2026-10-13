package com.medinexus.controller;

import com.medinexus.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application and database health monitoring")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Verifies Spring Boot API runtime and MySQL database connectivity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "UP");
        details.put("timestamp", Instant.now().toString());

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            details.put("database", "CONNECTED");
            details.put("databaseProduct", metaData.getDatabaseProductName());
            details.put("databaseVersion", metaData.getDatabaseProductVersion());
            details.put("databaseUrl", metaData.getURL());
        } catch (Exception ex) {
            log.error("Database health check error: {}", ex.getMessage());
            details.put("database", "DISCONNECTED");
            details.put("databaseError", ex.getMessage());
            return ResponseEntity.status(503).body(new ApiResponse<>(false, "MediNexus API Running (Database Disconnected)", details));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "MediNexus API Running", details));
    }
}
