package cc.mewcraft.extracontexts.common.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Database connection manager for ExtraContexts.
 *
 * Handles connection pooling and database initialization.
 */
object DatabaseManager {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var database: Database? = null

    /**
     * Initialize database connection with configuration object.
     *
     * @param config DatabaseConfiguration object
     */
    fun initialize(config: DatabaseConfiguration) {
        if (database != null) {
            logger.warn("Database already initialized, skipping re-initialization")
            return
        }

        try {
            logger.info("Initializing database connection...")
            logger.info("Database type: ${config.type}")

            // Get the appropriate connection provider for the database type
            val connectionProvider = DatabaseConnectionProviderFactory.create(config.type)
            val hikariConfig = connectionProvider.createHikariConfig(config)

            // Log connection details for debugging (hide password)
            logger.info("HikariCP configuration:")
            logger.info("  - JDBC URL: ${hikariConfig.jdbcUrl}")
            logger.info("  - Username: ${hikariConfig.username}")
            logger.info("  - Maximum pool size: ${hikariConfig.maximumPoolSize}")
            logger.info("  - Minimum idle: ${hikariConfig.minimumIdle}")

            val dataSource = HikariDataSource(hikariConfig)
            val databaseConfig = DatabaseConfig.Companion {
                useNestedTransactions = true
            }

            database = Database.connect(
                datasource = dataSource,
                databaseConfig = databaseConfig,
                connectionAutoRegistration = ExposedConnectionImpl() // 在 shade 环境下 Exposed 无法自动找到连接的实现, 需要我们自己指定
            )

            logger.info("Database connected successfully using ${config.type} database")
            logger.info("Connection pool initialized with max size: ${hikariConfig.maximumPoolSize}")

            transaction(database) {
                SchemaUtils.create(KeyValuePairs)
                logger.info("Database schema created/verified")
            }

        } catch (e: Exception) {
            logger.error("Failed to initialize database connection: ${e.message}", e)
            throw RuntimeException("Failed to initialize database connection", e)
        }
    }

    /**
     * Get the current database instance.
     */
    fun database(): Database {
        return requireNotNull(database) { "Database not initialized, call initialize() first!" }
    }
}