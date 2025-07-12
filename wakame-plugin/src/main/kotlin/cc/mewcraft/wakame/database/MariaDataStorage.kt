package cc.mewcraft.wakame.database

import cc.mewcraft.wakame.SharedConstants
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl

class MariaDataStorage(
    override val config: DataStorageConfig
) : DataStorage {
    override val adapter: DataAdapter = DataAdapter.MARIADB
    private lateinit var dataSource: HikariDataSource

    override fun connectToDatabase() {
        val credentials = config.credentials
        val config = commonConfig().apply {
            jdbcUrl = "jdbc:mariadb://${credentials.host}:${credentials.port}/${credentials.database}${credentials.parameters}"
            username = credentials.username
            password = credentials.password
        }

        dataSource = HikariDataSource(config)
        Database.connect(
            datasource = dataSource,
            setupConnection = { it.autoCommit = false },
            databaseConfig = DatabaseConfig {
                sqlLogger = if (SharedConstants.isRunningInIde) StdOutSqlLogger else null
            },
            connectionAutoRegistration = ExposedConnectionImpl() // 在 shade 环境下 Exposed 无法自动找到连接的实现, 需要我们自己指定.
        )
    }

    override fun closeDatabase() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}