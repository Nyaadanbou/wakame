package cc.mewcraft.extracontexts.common.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.*

/**
 * Exposed table definition for the table.
 *
 * Each row is uniquely identified by (id, key).
 *
 * The id field stores the UUID as a string (36 characters for standard UUID format).
 */
object KeyValuePairs : Table("kv_store") {
    /**
     * Player UUID as string (36 characters).
     * Stored as VARCHAR instead of UUID type for better cross-database compatibility.
     */
    val id = varchar("id", length = 36)

    /**
     * Logical key for the entry.
     */
    val key = varchar("key", length = 64)

    /**
     * Stored value as text.
     */
    val value = varchar("value", length = 64)

    /**
     * Primary key constraint on (id, key).
     */
    override val primaryKey: PrimaryKey = PrimaryKey(id, key, name = "pk")
}

/**
 * Helper functions for KeyValuePairsTable queries.
 */
object KeyValuePairsQueries {
    /**
     * Convert UUID to string for database queries.
     */
    private fun idOf(playerId: UUID): String {
        return playerId.toString()
    }

    /**
     * Find all entries for a specific player.
     */
    fun findByPlayer(playerId: UUID): Query {
        return KeyValuePairs
            .select(KeyValuePairs.key, KeyValuePairs.value)
            .where { KeyValuePairs.id eq idOf(playerId) }
    }

    /**
     * Find a specific entry by playerId and key.
     */
    fun findByPlayerAndKey(playerId: UUID, key: String): Query {
        return KeyValuePairs
            .select(KeyValuePairs.value)
            .where { (KeyValuePairs.id eq idOf(playerId)) and (KeyValuePairs.key eq key) }
    }

    /**
     * Upsert (insert or update) an entry.
     */
    fun upsertEntry(playerId: UUID, key: String, value: String) {
        KeyValuePairs.upsert {
            it[KeyValuePairs.id] = idOf(playerId)
            it[KeyValuePairs.key] = key
            it[KeyValuePairs.value] = value
        }
    }

    /**
     * Delete all entries for a specific player.
     */
    fun deleteByPlayer(playerId: UUID): Int {
        return KeyValuePairs
            .deleteWhere { KeyValuePairs.id eq idOf(playerId) }
    }

    /**
     * Delete a specific entry by playerId and key.
     */
    fun deleteByPlayerAndKey(playerId: UUID, key: String): Int {
        return KeyValuePairs
            .deleteWhere {
                (KeyValuePairs.id eq idOf(playerId)) and (KeyValuePairs.key eq key)
            }
    }

    /**
     * Check if an entry exists.
     */
    fun existsEntry(playerId: UUID, key: String): Boolean {
        return findByPlayerAndKey(playerId, key)
            .empty()
            .not()
    }

    /**
     * Find all entries for a specific player with keys starting with a given prefix.
     */
    fun findByPlayerAndKeyPrefix(playerId: UUID, prefix: String): Query {
        return KeyValuePairs
            .select(KeyValuePairs.key, KeyValuePairs.value)
            .where {
                (KeyValuePairs.id eq idOf(playerId)) and
                (KeyValuePairs.key like "$prefix%")
            }
    }

    /**
     * Delete all entries for a specific player with keys starting with a given prefix.
     */
    fun deleteByPlayerAndKeyPrefix(playerId: UUID, prefix: String): Int {
        return KeyValuePairs
            .deleteWhere {
                (KeyValuePairs.id eq idOf(playerId)) and
                (KeyValuePairs.key like "$prefix%")
            }
    }
}
