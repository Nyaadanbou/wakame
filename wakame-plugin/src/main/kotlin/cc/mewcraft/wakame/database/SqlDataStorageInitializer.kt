package cc.mewcraft.wakame.database

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import xyz.xenondevs.commons.provider.orElse

private val GLOBAL_DATABASE_CONFIG_FILE = Configs["database"]

@Init(stage = InitStage.PRE_WORLD)
object SqlDataStorageInitializer {
    private val adapter: SqlDataAdapter by GLOBAL_DATABASE_CONFIG_FILE.entry<SqlDataAdapter>("sql", "adapter").orElse(SqlDataAdapter.SQLITE)
    private val config: SqlDataStorageConfig by GLOBAL_DATABASE_CONFIG_FILE.entry("sql", "database")

    private lateinit var SqlDataStorage: SqlDataStorage

    @InitFun
    fun init() {
        SqlDataStorage = when (adapter) {
            SqlDataAdapter.SQLITE -> SqliteDataStorage(config)
            SqlDataAdapter.MARIADB -> MariaDataStorage(config)
        }
        SqlDataStorage.connectToDatabase()
    }

    @DisableFun
    fun disable() {
        if (::SqlDataStorage.isInitialized) {
            SqlDataStorage.closeDatabase()
        }
    }
}