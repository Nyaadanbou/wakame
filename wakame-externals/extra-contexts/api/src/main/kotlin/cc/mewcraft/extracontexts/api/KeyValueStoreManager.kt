package cc.mewcraft.extracontexts.api

import java.util.*

/**
 * Interface for managing key-value context entries.
 *
 * This provides a unified API for storing and retrieving arbitrary key-value pairs.
 */
interface KeyValueStoreManager {

    /**
     * Get all key-value pairs for a given player.
     *
     * @param id The UUID of the player
     * @return A set of all keys matching the prefix
     */
    fun get(id: UUID): List<Pair<String, String>>

    /**
     * Get a value from the key-value manager by key.
     *
     * @param id The UUID of the player
     * @param key The key to retrieve
     * @return The value associated with the key, or null if not found
     */
    fun get(id: UUID, key: String): String?

    /**
     * Get all key-value pairs with a given prefix for a player.
     *
     * @param id The UUID of the player
     * @param prefix The prefix of keys to retrieve
     * @return A set of all keys matching the prefix
     */
    fun getWithPrefix(id: UUID, prefix: String): List<Pair<String, String>>

    /**
     * Set a value in the key-value manager.
     *
     * @param id The UUID of the player
     * @param key The key to set
     * @param value The value to store
     */
    fun set(id: UUID, key: String, value: String)

    /**
     * Delete all key-value pairs for a given player.
     *
     * @param id The UUID of the player
     */
    fun delete(id: UUID)

    /**
     * Delete a key from the key-value manager.
     *
     * @param id The UUID of the player
     * @param key The key to delete
     */
    fun delete(id: UUID, key: String)

    /**
     * Delete all keys with a given prefix from the key-value manager.
     *
     * @param id The UUID of the player
     * @param prefix The prefix of keys to delete
     */
    fun deleteWithPrefix(id: UUID, prefix: String)

    /**
     * Check if a key exists in the key-value manager.
     *
     * @param id The UUID of the player
     * @param key The key to check
     * @return true if the key exists, false otherwise
     */
    fun exists(id: UUID, key: String): Boolean

    /**
     * Clear all entries in the storage.
     */
    fun clear()

    /**
     * This companion object provides a static access point to the current implementation of [KeyValueStoreManager].
     */
    companion object Implementation : KeyValueStoreManager {

        private var implementation: KeyValueStoreManager = object : KeyValueStoreManager {
            override fun get(id: UUID): List<Pair<String, String>> = emptyList()
            override fun get(id: UUID, key: String): String? = null
            override fun getWithPrefix(id: UUID, prefix: String): List<Pair<String, String>> = emptyList()
            override fun set(id: UUID, key: String, value: String) = Unit
            override fun delete(id: UUID) = Unit
            override fun delete(id: UUID, key: String) = Unit
            override fun deleteWithPrefix(id: UUID, prefix: String) = Unit
            override fun exists(id: UUID, key: String): Boolean = false
            override fun clear() = Unit
        }

        fun setImplementation(impl: KeyValueStoreManager) {
            implementation = impl
        }

        override fun get(id: UUID): List<Pair<String, String>> {
            return implementation.get(id) ?: emptyList()
        }

        override fun get(id: UUID, key: String): String? {
            return implementation.get(id, key)
        }

        override fun getWithPrefix(id: UUID, prefix: String): List<Pair<String, String>> {
            return implementation.getWithPrefix(id, prefix)
        }

        override fun set(id: UUID, key: String, value: String) {
            implementation.set(id, key, value)
        }

        override fun delete(id: UUID) {
            implementation.delete(id)
        }

        override fun delete(id: UUID, key: String) {
            implementation.delete(id, key)
        }

        override fun deleteWithPrefix(id: UUID, prefix: String) {
            implementation.deleteWithPrefix(id, prefix)
        }

        override fun exists(id: UUID, key: String): Boolean {
            return implementation.exists(id, key) ?: false
        }

        override fun clear() {
            implementation.clear()
        }
    }
}
