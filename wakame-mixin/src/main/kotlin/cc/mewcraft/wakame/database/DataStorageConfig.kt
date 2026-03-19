package cc.mewcraft.wakame.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 数据库连接凭证.
 *
 * 对于 SQLite, 仅 [filePath] 有效 (其他字段忽略).
 * 对于 MariaDB, 使用 [host], [port], [database], [username], [password], [parameters].
 */
@ConfigSerializable
data class DatabaseCredentials(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "koish",
    val username: String = "minecraft",
    val password: String = "",
    val parameters: String = "",
    /** SQLite 数据库文件名 (相对于数据目录, 例如 "sqlite.db"). 为空时使用默认值. */
    val filePath: String = "",
)

/**
 * 数据库连接池配置.
 */
@ConfigSerializable
data class DatabasePoolConfig(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 10,
    val maximumLifetime: Long = 1800000L,
    val keepAliveInterval: Long = 0L,
    val connectionTimeout: Long = 5000L,
)