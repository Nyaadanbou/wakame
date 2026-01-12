package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.common.database.DatabaseConfiguration
import cc.mewcraft.extracontexts.common.database.DatabaseType
import org.bukkit.plugin.java.JavaPlugin

/**
 * Configuration loader for Paper using Bukkit's configuration API.
 */
object ConfigurationLoader {

    /**
     * Load database configuration from file.
     *
     * Assumes the plugin has already called saveDefaultConfig() to initialize the file.
     *
     * @param plugin The JavaPlugin instance
     * @return [DatabaseConfiguration] loaded from file
     */
    fun loadConfiguration(plugin: JavaPlugin): DatabaseConfiguration {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        val config = plugin.config

        val databaseSection = config.getConfigurationSection("database")
            ?: throw IllegalArgumentException("Missing 'database' section in config")

        val typeString = databaseSection.getString("type", "MARIADB") ?: "MARIADB"
        val type = DatabaseType.fromString(typeString)

        val credentialsSection = databaseSection.getConfigurationSection("credentials")
            ?: throw IllegalArgumentException("Missing 'credentials' section in database config")

        val host = credentialsSection.getString("host", "localhost") ?: "localhost"
        val port = credentialsSection.getInt("port", 3306)
        val database = credentialsSection.getString("database", "database") ?: "database"
        val username = credentialsSection.getString("username", "root") ?: "root"
        val password = credentialsSection.getString("password", "") ?: ""
        val parameters = credentialsSection.getString("parameters")
        val filePath = credentialsSection.getString("filePath")

        val poolSection = databaseSection.getConfigurationSection("pool_options")
            ?: throw IllegalArgumentException("Missing 'pool_options' section in database config")

        val maximumPoolSize = poolSection.getInt("size", 10)
        val minimumIdle = poolSection.getInt("idle", 5)
        val maxLifetime = poolSection.getLong("lifetime", 1800000)
        val connectionTimeout = poolSection.getLong("timeout", 30000)
        val idleTimeout = poolSection.getLong("keep_alive", 30000)

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

