package cc.mewcraft.wakame.database

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.KoishSharedConstants
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.database.DatabaseManager.database
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl
import kotlin.io.path.createDirectories

private val DB_CONFIG = ConfigAccess["database"]

/**
 * 全局数据库连接管理器.
 *
 * 根据 `database.yml` 配置初始化 HikariCP 连接池和 Exposed [Database] 实例.
 * 其他需要数据库的模块通过 [database] 获取共享的连接实例.
 *
 * ### 使用方式
 *
 * ```
 * // 在任意模块中获取全局 Database 实例
 * val db = DatabaseManager.database()
 *
 * // 在 Exposed transaction 中使用
 * transaction(db) {
 *     MyTable.selectAll().where { ... }
 * }
 * ```
 */
@Init(InitStage.PRE_WORLD)
object DatabaseManager {

    private val type by DB_CONFIG.entryOrElse<DatabaseType>(DatabaseType.SQLITE, "type")
    private val credentials by DB_CONFIG.entryOrElse<DatabaseCredentials>(DatabaseCredentials(), "credentials")
    private val poolConfig by DB_CONFIG.entryOrElse<DatabasePoolConfig>(DatabasePoolConfig(), "connection_pool")

    private var dataSource: HikariDataSource? = null
    private var database: Database? = null

    /**
     * 获取全局 [Database] 实例.
     *
     * @throws IllegalStateException 如果数据库尚未初始化
     */
    fun database(): Database {
        return requireNotNull(database) { "Database not initialized! Is the database module enabled?" }
    }

    @InitFun
    fun init() {
        LOGGER.info("[Database] Initializing database connection... (type=$type)")

        val hikariConfig = createHikariConfig()
        val ds = HikariDataSource(hikariConfig)
        dataSource = ds

        database = Database.connect(
            datasource = ds,
            setupConnection = { it.autoCommit = false },
            databaseConfig = DatabaseConfig {
                sqlLogger = if (KoishSharedConstants.isRunningInIde) StdOutSqlLogger else null
            },
            connectionAutoRegistration = ExposedConnectionImpl(),
        )

        LOGGER.info("[Database] Database connected. (type=$type)")
    }

    @DisableFun
    fun disable() {
        LOGGER.info("[Database] Closing database connection...")

        dataSource?.close()
        dataSource = null
        database = null

        LOGGER.info("[Database] Database connection closed.")
    }

    // ================================================================
    //  Internal
    // ================================================================

    private fun createHikariConfig(): HikariConfig {
        return HikariConfig().apply {
            maximumPoolSize = poolConfig.maximumPoolSize
            minimumIdle = poolConfig.minimumIdle
            maxLifetime = poolConfig.maximumLifetime
            keepaliveTime = poolConfig.keepAliveInterval
            connectionTimeout = poolConfig.connectionTimeout

            when (type) {
                DatabaseType.SQLITE -> {
                    driverClassName = "org.sqlite.JDBC"
                    val fileName = credentials.filePath.ifEmpty { "sqlite.db" }
                    val path = KoishDataPaths.DATA.also { it.createDirectories() }.resolve(fileName)
                    jdbcUrl = "jdbc:sqlite:${path.toAbsolutePath()}"
                }

                DatabaseType.MARIADB -> {
                    driverClassName = "org.mariadb.jdbc.Driver"
                    val cred = credentials
                    jdbcUrl = "jdbc:mariadb://${cred.host}:${cred.port}/${cred.database}${cred.parameters}"
                    username = cred.username
                    password = cred.password
                }
            }
        }
    }
}

