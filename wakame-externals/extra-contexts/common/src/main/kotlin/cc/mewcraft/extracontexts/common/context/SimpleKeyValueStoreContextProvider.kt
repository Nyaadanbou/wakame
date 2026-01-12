package cc.mewcraft.extracontexts.common.context

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import cc.mewcraft.extracontexts.common.storage.SimpleKeyValueStoreManager
import java.util.*

/**
 * Default implementation of KVStoreContextProvider using Exposed ORM.
 * Manages LuckPerms contexts backed by the KVStore database.
 */
class SimpleKeyValueStoreContextProvider : KeyValueStoreContextProvider {

    override fun registerContext(id: UUID, key: String, value: String) {
        SimpleKeyValueStoreManager.set(id, key, value)
    }

    override fun unregisterContext(id: UUID, key: String) {
        SimpleKeyValueStoreManager.delete(id, key)
    }

    override fun getContexts(id: UUID): List<Pair<String, String>> {
        return SimpleKeyValueStoreManager.get(id)
    }

    override fun clearContexts(id: UUID) {
        SimpleKeyValueStoreManager.delete(id)
    }
}
