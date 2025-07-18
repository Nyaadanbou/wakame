package cc.mewcraft.wakame.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SqlDataStorageConfig(
    val credentials: SqlDataStorageCredentials,
    val connectionPool: SqlDataStorageConnectionPool = SqlDataStorageConnectionPool(),
)

@ConfigSerializable
data class SqlDataStorageCredentials(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val parameters: String = "",
)

@ConfigSerializable
data class SqlDataStorageConnectionPool(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 10,
    val maximumLifetime: Long = 1800000L,
    val keepAliveInterval: Long = 0L,
    val connectionTimeout: Long = 5000L,
)