package cc.mewcraft.extracontexts.velocity

import cc.mewcraft.extracontexts.common.database.DatabaseConfiguration
import cc.mewcraft.extracontexts.common.database.DatabaseType
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * Configuration loader for Velocity using Configurate YAML.
 */
object ConfigurationLoader {

    /**
     * Load database configuration from file.
     *
     * @param configPath The path to the config file
     * @return [DatabaseConfiguration] loaded from file
     */
    fun loadConfiguration(configPath: Path): DatabaseConfiguration {
        val rootNode = YamlConfigurationLoader.builder()
            .path(configPath)
            .buildAndLoadString(configPath.readText())

        val databaseNode = rootNode.node("database")

        // Load database type
        val typeString = databaseNode.node("type").getString("MARIADB")
        val type = DatabaseType.fromString(typeString)

        // Load credentials
        val credentialsNode = databaseNode.node("credentials")
        val host = credentialsNode.node("host").getString("localhost")
        val port = credentialsNode.node("port").getInt(3306)
        val database = credentialsNode.node("database").getString("database")
        val username = credentialsNode.node("username").getString("username")
        val password = credentialsNode.node("password").getString("password")
        val parameters = credentialsNode.node("parameters").string
        val filePath = credentialsNode.node("filePath").string

        // Load pool options
        val poolNode = databaseNode.node("pool_options")
        val maximumPoolSize = poolNode.node("size").getInt(10)
        val minimumIdle = poolNode.node("idle").getInt(5)
        val connectionTimeout = poolNode.node("timeout").getLong(30000)
        val idleTimeout = poolNode.node("keep_alive").getLong(30000)
        val maxLifetime = poolNode.node("lifetime").getLong(1800000)

        return DatabaseConfiguration(
            type = type,
            host = host,
            port = port,
            database = database,
            username = username,
            password = password,
            parameters = parameters,
            filePath = filePath,
            maximumPoolSize = maximumPoolSize,
            minimumIdle = minimumIdle,
            connectionTimeout = connectionTimeout,
            idleTimeout = idleTimeout,
            maxLifetime = maxLifetime
        )
    }
}

