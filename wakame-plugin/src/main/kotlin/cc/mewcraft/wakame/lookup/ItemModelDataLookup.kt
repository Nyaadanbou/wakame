package cc.mewcraft.wakame.lookup

import net.kyori.adventure.key.Key

class ItemModelDataLookup {
    // K: WakaItem's key
    // V: Custom Model Data
    private val cache = HashMap<Key, Int>()

    fun put(key: Key, id: Int) {

    }

    fun get(key: Key): Int {
        return cache[key] ?: throw NullPointerException(key.asString())
    }
}