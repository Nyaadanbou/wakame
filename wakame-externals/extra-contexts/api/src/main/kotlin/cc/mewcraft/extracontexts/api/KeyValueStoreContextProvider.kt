package cc.mewcraft.extracontexts.api

import java.util.*

/**
 * Context provider for KVStore contexts in LuckPerms.
 *
 * This allows plugins to register and manage KVStore-based contexts.
 */
interface KeyValueStoreContextProvider {

    /**
     * Register a KVStore context for a player.
     *
     * @param id The UUID of the player
     * @param key The context key
     * @param value The context value
     */
    fun registerContext(id: UUID, key: String, value: String)

    /**
     * Unregister a KVStore context for a player.
     *
     * @param id The UUID of the player
     * @param key The context key
     */
    fun unregisterContext(id: UUID, key: String)

    /**
     * Get all contexts for a player.
     *
     * @param id The UUID of the player
     * @return A list of all contexts for the player
     */
    fun getContexts(id: UUID): List<Pair<String, String>>

    /**
     * Clear all contexts for a player.
     *
     * @param id The UUID of the player
     */
    fun clearContexts(id: UUID)

    /**
     * This companion object provides a static access point to the current implementation of [KeyValueStoreContextProvider].
     */
    companion object Implementation : KeyValueStoreContextProvider {

        private var implementation: KeyValueStoreContextProvider = object : KeyValueStoreContextProvider {
            override fun registerContext(id: UUID, key: String, value: String) = Unit
            override fun unregisterContext(id: UUID, key: String) = Unit
            override fun getContexts(id: UUID): List<Pair<String, String>> = emptyList()
            override fun clearContexts(id: UUID) = Unit
        }

        fun setImplementation(impl: KeyValueStoreContextProvider) {
            implementation = impl
        }

        override fun registerContext(id: UUID, key: String, value: String) {
            implementation.registerContext(id, key, value)
        }

        override fun unregisterContext(id: UUID, key: String) {
            implementation.unregisterContext(id, key)
        }

        override fun getContexts(id: UUID): List<Pair<String, String>> {
            return implementation.getContexts(id)
        }

        override fun clearContexts(id: UUID) {
            implementation.clearContexts(id)
        }
    }
}

