package cc.mewcraft.wakame.registry

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

object RarityRegistry {
    private val indexMappings: BiMap<Byte, String> = HashBiMap.create(8)

    fun getIdentity(name: String): Byte {
        return indexMappings.inverse()[name] ?: error("Cannot find identity from $name")
    }

    fun getName(id: Byte): String {
        return indexMappings[id] ?: error("Cannot find name from $id")
    }
}