package cc.mewcraft.extracontexts.common.database

import com.zaxxer.hikari.HikariConfig

/**
 * Interface for providing database connections based on database type.
 */
interface DatabaseConnectionProvider {
    /**
     * Create a HikariConfig for the database connection.
     */
    fun createHikariConfig(config: DatabaseConfiguration): HikariConfig
}

/**
 * SQLite database connection provider.
 */
object SqliteConnectionProvider : DatabaseConnectionProvider {
    override fun createHikariConfig(config: DatabaseConfiguration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.sqlite.JDBC"
            jdbcUrl = "jdbc:sqlite:${config.credentials.filePath ?: "sqlite.db"}"
            maximumPoolSize = config.poolOptions.size
            minimumIdle = config.poolOptions.idle
            connectionTimeout = config.poolOptions.timeout
            idleTimeout = config.poolOptions.keepAlive
            maxLifetime = config.poolOptions.lifetime
        }
    }
}

/**
 * H2 database connection provider.
 */
object H2ConnectionProvider : DatabaseConnectionProvider {
    override fun createHikariConfig(config: DatabaseConfiguration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            // DB_CLOSE_DELAY=-1 keeps the database open and prevents data loss when transactions close
            // This is especially important for in-memory databases (mem:) to persist data across multiple transactions
            jdbcUrl = "jdbc:h2:${config.credentials.filePath ?: "h2"};DB_CLOSE_DELAY=-1"
            maximumPoolSize = config.poolOptions.size
            minimumIdle = config.poolOptions.idle
            connectionTimeout = config.poolOptions.timeout
            idleTimeout = config.poolOptions.keepAlive
            maxLifetime = config.poolOptions.lifetime
        }
    }
}

/**
 * MySQL database connection provider.
 */
object MysqlConnectionProvider : DatabaseConnectionProvider {
    override fun createHikariConfig(config: DatabaseConfiguration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            val params = config.credentials.parameters ?: "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
            jdbcUrl = "jdbc:mysql://${config.credentials.host}:${config.credentials.port}/${config.credentials.database}$params"
            username = config.credentials.username
            password = config.credentials.password
            maximumPoolSize = config.poolOptions.size
            minimumIdle = config.poolOptions.idle
            connectionTimeout = config.poolOptions.timeout
            idleTimeout = config.poolOptions.keepAlive
            maxLifetime = config.poolOptions.lifetime
        }
    }
}

/**
 * MariaDB database connection provider.
 */
object MariadbConnectionProvider : DatabaseConnectionProvider {
    override fun createHikariConfig(config: DatabaseConfiguration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.mariadb.jdbc.Driver"
            val params = config.credentials.parameters ?: "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
            jdbcUrl = "jdbc:mariadb://${config.credentials.host}:${config.credentials.port}/${config.credentials.database}$params"
            username = config.credentials.username
            password = config.credentials.password
            maximumPoolSize = config.poolOptions.size
            minimumIdle = config.poolOptions.idle
            connectionTimeout = config.poolOptions.timeout
            idleTimeout = config.poolOptions.keepAlive
            maxLifetime = config.poolOptions.lifetime
        }
    }
}

/**
 * PostgreSQL database connection provider.
 */
object PostgresqlConnectionProvider : DatabaseConnectionProvider {
    override fun createHikariConfig(config: DatabaseConfiguration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            val params = config.credentials.parameters ?: "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
            jdbcUrl = "jdbc:postgresql://${config.credentials.host}:${config.credentials.port}/${config.credentials.database}$params"
            username = config.credentials.username
            password = config.credentials.password
            maximumPoolSize = config.poolOptions.size
            minimumIdle = config.poolOptions.idle
            connectionTimeout = config.poolOptions.timeout
            idleTimeout = config.poolOptions.keepAlive
            maxLifetime = config.poolOptions.lifetime
        }
    }
}

/**
 * Factory for creating database connection providers.
 */
object DatabaseConnectionProviderFactory {
    fun create(type: DatabaseType): DatabaseConnectionProvider {
        return when (type) {
            DatabaseType.SQLITE -> SqliteConnectionProvider
            DatabaseType.H2 -> H2ConnectionProvider
            DatabaseType.MYSQL -> MysqlConnectionProvider
            DatabaseType.MARIADB -> MariadbConnectionProvider
            DatabaseType.POSTGRESQL -> PostgresqlConnectionProvider
        }
    }
}

