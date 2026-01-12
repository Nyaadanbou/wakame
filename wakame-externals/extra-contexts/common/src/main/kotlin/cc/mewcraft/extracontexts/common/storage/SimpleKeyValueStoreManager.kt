package cc.mewcraft.extracontexts.common.storage

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.database.DatabaseManager
import cc.mewcraft.extracontexts.common.database.KeyValuePairs
import cc.mewcraft.extracontexts.common.database.KeyValuePairsQueries
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

/**
 * Default implementation of KeyValueStoreManager using Exposed ORM with query helper functions.
 *
 * Uses KeyValuePairsQueries to centralize UUID to String conversion logic.
 * No need for manual .toString() calls in the manager.
 */
object SimpleKeyValueStoreManager : KeyValueStoreManager {

    override fun get(id: UUID): List<Pair<String, String>> {
        return transaction(DatabaseManager.database()) {
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .findByPlayer(id)
                .map { row -> row[KeyValuePairs.key] to row[KeyValuePairs.value] }
                .toList()
        }
    }

    override fun get(id: UUID, key: String): String? {
        return transaction(DatabaseManager.database()) {
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .findByPlayerAndKey(id, key)
                .singleOrNull()
                ?.get(KeyValuePairs.value)
        }
    }

    override fun set(id: UUID, key: String, value: String) {
        transaction(DatabaseManager.database()) {
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .upsertEntry(id, key, value)
        }
    }

    override fun delete(id: UUID) {
        transaction(DatabaseManager.database()) unit@{
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .deleteByPlayer(id)
        }
    }

    override fun delete(id: UUID, key: String) {
        transaction(DatabaseManager.database()) unit@{
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .deleteByPlayerAndKey(id, key)
        }
    }

    override fun exists(id: UUID, key: String): Boolean {
        return transaction(DatabaseManager.database()) {
            addLogger(StdOutSqlLogger)
            KeyValuePairsQueries
                .existsEntry(id, key)
        }
    }

    override fun clear() {
        transaction(DatabaseManager.database()) {
            addLogger(StdOutSqlLogger)
            KeyValuePairs
                .deleteAll()
        }
    }
}

