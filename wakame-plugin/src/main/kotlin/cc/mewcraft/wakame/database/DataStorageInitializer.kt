package cc.mewcraft.wakame.database

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import com.zaxxer.hikari.HikariConfig
import xyz.xenondevs.commons.provider.orElse

private val GLOBAL_DATABASE_CONFIG_FILE = ConfigAccess["database"]

@Init(InitStage.PRE_WORLD)
object DataStorageInitializer {
    private val adapter: DataAdapter by GLOBAL_DATABASE_CONFIG_FILE.entry<DataAdapter>("adapter").orElse(DataAdapter.SQLITE)
    private val config: DataStorageConfig by GLOBAL_DATABASE_CONFIG_FILE.entry("database")

    private lateinit var dataStorage: DataStorage

    @InitFun
    fun init() {
        dataStorage = when (adapter) {
            DataAdapter.SQLITE -> SQLiteDataStorage(config)
            DataAdapter.MARIADB -> MariaDataStorage(config)
        }
        dataStorage.connectToDatabase()
    }

    @DisableFun
    fun disable() {
        if (::dataStorage.isInitialized) {
            dataStorage.closeDatabase()
        }
    }
}

internal fun DataStorage.commonConfig(): HikariConfig {
    val connectionPool = config.connectionPool
    return HikariConfig().apply {
        maximumPoolSize = connectionPool.maximumPoolSize
        minimumIdle = connectionPool.minimumIdle
        maxLifetime = connectionPool.maximumLifetime
        keepaliveTime = connectionPool.keepAliveInterval
        connectionTimeout = connectionPool.connectionTimeout
    }
}