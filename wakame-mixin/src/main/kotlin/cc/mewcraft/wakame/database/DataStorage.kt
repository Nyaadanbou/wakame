package cc.mewcraft.wakame.database

/**
 * 代表一个数据存储接口, 用于连接和操作数据库.
 */
interface DataStorage {

    /**
     * 数据适配器, 用于标识当前使用的数据库类型.
     */
    val adapter: DataAdapter

    /**
     * 数据库配置, 包含连接信息和其他相关设置.
     */
    val config: DataStorageConfig

    /**
     * 连接到数据库, 初始化数据源并设置相关配置.
     */
    fun connectToDatabase()

    /**
     * 关闭数据库连接, 释放资源.
     */
    fun closeDatabase()
}