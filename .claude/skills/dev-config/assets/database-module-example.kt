// 完整示例: 功能模块配置
// 来源: dev-config SKILL.md §6

// ========== 配置数据类 (可放在 wakame-mixin 或 wakame-plugin) ==========

@ConfigSerializable
data class DatabaseCredentials(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "koish",
    val username: String = "minecraft",
    val password: String = "",
    val parameters: String = "",
    val filePath: String = "",             // -> file_path
)

@ConfigSerializable
data class DatabasePoolConfig(
    val maximumPoolSize: Int = 10,         // -> maximum_pool_size
    val minimumIdle: Int = 10,             // -> minimum_idle
    val maximumLifetime: Long = 1800000L,  // -> maximum_lifetime
    val keepAliveInterval: Long = 0L,      // -> keep_alive_interval
    val connectionTimeout: Long = 5000L,   // -> connection_timeout
)

// ========== 功能实现 (wakame-plugin) ==========

private val DB_CONFIG = ConfigAccess["database"]  // -> configs/database.yml

@Init(InitStage.PRE_WORLD)
object DatabaseManager {

    private val type by DB_CONFIG.entryOrElse<DatabaseType>(DatabaseType.SQLITE, "type")
    private val credentials by DB_CONFIG.entryOrElse<DatabaseCredentials>(DatabaseCredentials(), "credentials")
    private val poolConfig by DB_CONFIG.entryOrElse<DatabasePoolConfig>(DatabasePoolConfig(), "connection_pool")

    @InitFun
    fun init() {
        // 直接使用 type, credentials, poolConfig
        // 配置重载时这些值会自动更新
    }
}
