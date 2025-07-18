package cc.mewcraft.wakame.database

import com.zaxxer.hikari.HikariConfig

/**
 * 代表一个数据存储接口, 用于连接和操作数据库.
 */
interface SqlDataStorage {

    /**
     * 数据适配器, 用于标识当前使用的数据库类型.
     */
    val adapter: SqlDataAdapter

    /**
     * 数据库配置, 包含连接信息和其他相关设置.
     */
    val config: SqlDataStorageConfig

    /**
     * 连接到数据库, 初始化数据源并设置相关配置.
     */
    fun connectToDatabase()

    /**
     * 关闭数据库连接, 释放资源.
     */
    fun closeDatabase()

    fun commonConfig(): HikariConfig {
        val connectionPool = config.connectionPool
        return HikariConfig().apply {
            maximumPoolSize = connectionPool.maximumPoolSize
            minimumIdle = connectionPool.minimumIdle
            maxLifetime = connectionPool.maximumLifetime
            keepaliveTime = connectionPool.keepAliveInterval
            connectionTimeout = connectionPool.connectionTimeout
        }
    }
}