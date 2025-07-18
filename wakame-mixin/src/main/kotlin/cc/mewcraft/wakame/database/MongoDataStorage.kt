package cc.mewcraft.wakame.database

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.jetbrains.annotations.ApiStatus

interface MongoDataStorage {
    companion object {
        private var database: MongoDatabase? = null

        /**
         * 获取当前的 MongoDB 数据库实例.
         *
         * @return 当前的 MongoDB 数据库实例.
         */
        fun database(): MongoDatabase {
            return database ?: throw IllegalStateException("MongoDB client is not initialized.")
        }

        @ApiStatus.Internal
        fun setDatabase(db: MongoDatabase?) {
            database = db
        }
    }
}