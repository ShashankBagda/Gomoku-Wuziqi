package com.goody.nus.se.gomoku.mongo;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * MongoDB Connection Test
 * Simple test to verify MongoDB connectivity
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@Disabled("Disabled until MongoDB server is available for testing")
@SpringBootTest(classes = MongoTestApplication.class)
@ConfigurationProperties
class MongoConnectionTest {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void testMongoClientConnection() {
        assertNotNull(mongoClient, "MongoClient should not be null");

        try {
            // Test connection by listing databases
            var databases = mongoClient.listDatabaseNames();
            assertNotNull(databases, "Should be able to list databases");

            System.out.println("=== Connected to MongoDB successfully ===");
            databases.forEach(db -> System.out.println("Database: " + db));
        } catch (Exception e) {
            fail("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    @Test
    void testMongoTemplateConnection() {
        assertNotNull(mongoTemplate, "MongoTemplate should not be null");

        try {
            // Test connection by getting collection names
            var collections = mongoTemplate.getCollectionNames();
            assertNotNull(collections, "Should be able to get collection names");

            System.out.println("=== MongoTemplate working successfully ===");
            System.out.println("Database: " + mongoTemplate.getDb().getName());
            collections.forEach(col -> System.out.println("Collection: " + col));
        } catch (Exception e) {
            fail("MongoTemplate connection failed: " + e.getMessage());
        }
    }

    @Test
    void testPingDatabase() {
        try {
            // Ping the database to verify connection
            var result = mongoTemplate.executeCommand("{ ping: 1 }");
            assertNotNull(result);
            System.out.println("=== Ping Result ===");
            System.out.println(result.toJson());
        } catch (Exception e) {
            fail("Ping failed: " + e.getMessage());
        }
    }
}
