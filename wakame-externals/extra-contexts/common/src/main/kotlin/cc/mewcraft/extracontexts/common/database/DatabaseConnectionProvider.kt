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
            jdbcUrl = "jdbc:sqlite:${config.filePath ?: "sqlite.db"}"
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime
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
            jdbcUrl = "jdbc:h2:${config.filePath ?: "extracontexts"}"
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime
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
            val params = config.parameters ?: ""
            jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}$params"
            username = config.username
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime
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
            val params = config.parameters ?: ""
            jdbcUrl = "jdbc:mariadb://${config.host}:${config.port}/${config.database}$params"
            username = config.username
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime
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
            val params = config.parameters ?: ""
            jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}$params"
            username = config.username
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime
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

