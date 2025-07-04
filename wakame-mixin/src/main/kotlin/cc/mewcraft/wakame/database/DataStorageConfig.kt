package cc.mewcraft.wakame.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class DataStorageConfig(
    val credentials: DataStorageCredentials,
    val connectionPool: DataStorageConnectionPool = DataStorageConnectionPool(),
)

@ConfigSerializable
data class DataStorageCredentials(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val parameters: String = "",
)

@ConfigSerializable
data class DataStorageConnectionPool(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 10,
    val maximumLifetime: Long = 1800000L,
    val keepAliveInterval: Long = 0L,
    val connectionTimeout: Long = 5000L,
)