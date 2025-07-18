package cc.mewcraft.wakame.database

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import com.mongodb.kotlin.client.coroutine.MongoClient

private val GLOBAL_DATABASE_CONFIG_FILE = Configs["database"]

@Init(stage = InitStage.PRE_WORLD)
object MongoDataStorageInitializer {
    private val config: MongoDataStorageConfig by GLOBAL_DATABASE_CONFIG_FILE.entry("mongodb", "database")

    private lateinit var mongoClient: MongoClient

    @InitFun
    fun init() {
        val credentials = config.credentials
        mongoClient = MongoClient.create(credentials.uri)
        val database = mongoClient.getDatabase(credentials.database)
        MongoDataStorage.setDatabase(database)
    }

    @DisableFun
    fun disable() {
        if (::mongoClient.isInitialized) {
            mongoClient.close()
        }
    }
}