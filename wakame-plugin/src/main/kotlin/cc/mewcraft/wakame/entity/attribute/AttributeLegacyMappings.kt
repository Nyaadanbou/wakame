package cc.mewcraft.wakame.entity.attribute

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import net.kyori.adventure.key.Key
import java.util.*

/**
 * 一个临时的实现, 用于将 [Key] 映射到 [UUID], 或者反过来.
 *
 * 该实现保证相同的 [Key] 会映射到相同的 [UUID]; 反过来亦是如此.
 */
object AttributeLegacyMappings {
    private val mappings: BiMap<Key, UUID> = HashBiMap.create()

    fun byId(id: UUID): Key {
        return mappings.inverse().computeIfAbsent(id) { generateName(it.toString()) }
    }

    fun byName(id: Key): UUID {
        return mappings.computeIfAbsent(id) { generateUUID(it.asString()) }
    }

    private fun generateName(name: String): Key {
        return Key.key(UUID.nameUUIDFromBytes(name.toByteArray()).toString())
    }

    private fun generateUUID(name: String): UUID {
        return UUID.nameUUIDFromBytes(name.toByteArray())
    }
}