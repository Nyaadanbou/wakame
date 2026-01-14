package cc.mewcraft.extracontexts.common.database

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.xenondevs.commons.provider.Provider


/* 接口 */


interface DatabaseConfiguration {
    val type: DatabaseType
    val credentials: DatabaseCredentials
    val poolOptions: DatabasePoolOptions
}

@ConfigSerializable
data class DatabaseCredentials(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "database",
    val username: String = "username",
    val password: String = "password",
    val parameters: String? = null,
    val filePath: String? = null,
)

@ConfigSerializable
data class DatabasePoolOptions(
    // Maximum number of connections in the pool
    val size: Int = 10,
    // Minimum number of idle connections in the pool
    val idle: Int = 5,
    // Maximum lifetime of a connection in milliseconds
    val lifetime: Long = 1800000,
    // Keep-alive time for idle connections in milliseconds
    val keepAlive: Long = 30000,
    // Connection timeout in milliseconds
    val timeout: Long = 30000,
)


/* 实现 */


@ConfigSerializable
data class ImmutableDatabaseConfiguration(
    override val type: DatabaseType = DatabaseType.H2,
    override val credentials: DatabaseCredentials = DatabaseCredentials(),
    override val poolOptions: DatabasePoolOptions = DatabasePoolOptions(),
) : DatabaseConfiguration

class ReactiveDatabaseConfiguration(
    provider: Provider<CommentedConfigurationNode>,
) : DatabaseConfiguration {
    private val database by provider.entryOrElse<ImmutableDatabaseConfiguration>(ImmutableDatabaseConfiguration(), "database")

    override val type: DatabaseType
        get() = database.type
    override val credentials: DatabaseCredentials
        get() = database.credentials
    override val poolOptions: DatabasePoolOptions
        get() = database.poolOptions
}
