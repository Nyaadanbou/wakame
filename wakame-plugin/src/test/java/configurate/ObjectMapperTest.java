package configurate;

import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        boolean disabled;

        @PostProcess
        void callback() {
            disabled = true;
        }
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
        assertTrue(config.disabled);

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

    @ConfigSerializable
    static class MapNodeFromParent {
        @Setting(nodeFromParent = true)
        Map<String, Integer> data;
    }


    @Test
    void testMapNodeFromParent() throws Exception {
        final ConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults());
        root.node("a").set(1);
        root.node("b").set(2);

        final MapNodeFromParent config = root.get(MapNodeFromParent.class);
        assertNotNull(config);
        assertEquals(2, config.data.size());
        assertEquals(1, config.data.get("a"));
        assertEquals(2, config.data.get("b"));
    }

    @ConfigSerializable
    static class MergeNodeFromParent {
        @Setting
        String type;

        @Setting(nodeFromParent = true)
        ApiConfig apiConfig;
    }

    @Test
    void testMergeNodeFromParent() throws Exception {
        final ConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults());
        root.node("type").set("merge");
        root.node("endpoint").set("https://api.example.com");
        root.node("api-key").set("abcdef123456");

        final MergeNodeFromParent config = root.get(MergeNodeFromParent.class);
        assertNotNull(config);
        assertEquals("merge", config.type);
        assertNotNull(config.apiConfig);
        assertEquals("https://api.example.com", config.apiConfig.endpoint);
        assertEquals("abcdef123456", config.apiConfig.apiKey);
    }

    enum EnumAsNodeKey {
        K1, K2, K3
    }

    @Test
    void testEnumAsNodeKey() throws Exception {
        final ConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults());
        root.node("k1").set("value1");
        root.node("k2").set("value2");
        root.node("k3").set("value3");

        final Map<EnumAsNodeKey, String> enumAsNodeKeyStringMap = root.get(new TypeToken<>() {});
        assertNotNull(enumAsNodeKeyStringMap);
        assertEquals(3, enumAsNodeKeyStringMap.size());
        assertEquals("value1", enumAsNodeKeyStringMap.get(EnumAsNodeKey.K1));
        assertEquals("value2", enumAsNodeKeyStringMap.get(EnumAsNodeKey.K2));
        assertEquals("value3", enumAsNodeKeyStringMap.get(EnumAsNodeKey.K3));
    }
}