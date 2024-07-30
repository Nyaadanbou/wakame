package cc.mewcraft.wakame.attribute

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * 一个临时的实现, 用于将 [Key] 映射到 [UUID], 或者反过来.
 *
 * 该实现保证相同的 [Key] 会映射到相同的 [UUID]; 反过来亦是如此.
 */
object AttributeLegacyMappings {
    private val mappings: BiMap<Key, UUID> = HashBiMap.create()

    fun byKey(id: Key): UUID {
        return mappings.computeIfAbsent(id) { generateUUID(it.asString()) }
    }

    fun byId(id: UUID): Key {
        return mappings.inverse().computeIfAbsent(id) { generateKey(it.toString()) }
    }

    private fun generateUUID(name: String): UUID {
        return UUID.nameUUIDFromBytes(name.toByteArray())
    }

    private fun generateKey(name: String): Key {
        return Key.key(UUID.nameUUIDFromBytes(name.toByteArray()).toString())
    }
}