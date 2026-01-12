package cc.mewcraft.extracontexts.common.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Configuration data class for database connection.
 */
data class DatabaseConfiguration(
    val type: DatabaseType = DatabaseType.MARIADB,
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "database",
    val username: String = "root",
    val password: String = "",
    val parameters: String? = null,
    val filePath: String? = null,
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 5,
    val connectionTimeout: Long = 30000,
    val idleTimeout: Long = 600000,
    val maxLifetime: Long = 1800000,
)

/**
 * Database connection manager for ExtraContexts.
 *
 * Handles connection pooling and database initialization.
 */
object DatabaseManager {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var database: Database? = null

    /**
     * Initialize database connection with custom settings (for backward compatibility).
     *
     * @param host Database host
     * @param port Database port
     * @param database Database name
     * @param username Database username
     * @param password Database password
     */
    fun initialize(host: String, port: Int, database: String, username: String, password: String) {
        initialize(DatabaseConfiguration(host = host, port = port, database = database, username = username, password = password))
    }

    /**
     * Initialize database connection with default MariaDB settings.
     *
     * This assumes a MariaDB instance is running at localhost:3306 with a database named "extracontexts".
     */
    fun initializeDefault() {
        initialize(DatabaseConfiguration())
    }

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
            logger.debug("HikariCP configuration:")
            logger.debug("  - JDBC URL: ${hikariConfig.jdbcUrl}")
            logger.debug("  - Username: ${hikariConfig.username}")
            logger.debug("  - Maximum pool size: ${hikariConfig.maximumPoolSize}")
            logger.debug("  - Minimum idle: ${hikariConfig.minimumIdle}")

            val dataSource = HikariDataSource(hikariConfig)
            val databaseConfig = DatabaseConfig {
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
