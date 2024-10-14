package configurate;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectMapperTest {

    @ConfigSerializable
    static class DatabaseConfig {
        @Setting String url;
        @Setting String username;
        @Setting String password;
    }

    @ConfigSerializable
    static class ApiConfig {
        @Setting String endpoint;
        @Setting String apiKey;
    }

    @ConfigSerializable
    static class AppConfig {
        @Setting DatabaseConfig database;
        @Setting ApiConfig api;
        @NodeKey String key;
    }

    @Test
    void testComplexConfig() throws Exception {
        final ConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults());

        // Setting up database configuration
        final ConfigurationNode app = root.node("app");
        app.node("database", "url").set("jdbc:mysql://localhost:3306/mydb");
        app.node("database", "username").set("admin");
        app.node("database", "password").set("secret");

        // Setting up API configuration
        app.node("api", "endpoint").set("https://api.example.com");
        app.node("api", "api-key").set("abcdef123456");

        // Loading the configuration into AppConfig
        final AppConfig config = root.node("app").get(AppConfig.class);
        assertNotNull(config);

        // Asserting database configuration
        assertEquals("jdbc:mysql://localhost:3306/mydb", config.database.url);
        assertEquals("admin", config.database.username);
        assertEquals("secret", config.database.password);

        // Asserting API configuration
        assertEquals("https://api.example.com", config.api.endpoint);
        assertEquals("abcdef123456", config.api.apiKey);
    }

    @Test
    void testLayeredConfigManagement() throws Exception {
        final ConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults());

        // Setting up global configuration
        root.node("database", "url").set("jdbc:mysql://global:3306/default");
        root.node("api", "endpoint").set("https://global.api.com");

        // Setting up local configuration
        root.node("local", "database", "url").set("jdbc:mysql://local:3306/mydb");
        root.node("local", "api", "endpoint").set("https://local.api.com");

        // Loading the local configuration into AppConfig
        final AppConfig localConfig = root.node("local").get(AppConfig.class);
        assertNotNull(localConfig);

        // Asserting local configuration overrides global configuration
        assertEquals("jdbc:mysql://local:3306/mydb", localConfig.database.url);
        assertEquals("https://local.api.com", localConfig.api.endpoint);

        // Asserting global configuration is still accessible
        final AppConfig globalConfig = root.get(AppConfig.class);
        assertNotNull(globalConfig);
        assertEquals("jdbc:mysql://global:3306/default", globalConfig.database.url);
        assertEquals("https://global.api.com", globalConfig.api.endpoint);
    }
}