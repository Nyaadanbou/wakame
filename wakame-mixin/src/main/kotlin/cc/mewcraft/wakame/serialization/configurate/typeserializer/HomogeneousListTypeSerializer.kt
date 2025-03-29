package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.applyIfNull
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.Keyed
import org.bukkit.Registry
import org.bukkit.block.BlockType
import org.bukkit.inventory.ItemType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

object BlockTypeListTypeSerializer : HomogeneousTypeListTypeSerializer<BlockType>(RegistryKey.BLOCK)

object ItemTypeListTypeSerializer : HomogeneousTypeListTypeSerializer<ItemType>(RegistryKey.ITEM)

sealed class HomogeneousTypeListTypeSerializer<T : Keyed>(
    private val registryKey: RegistryKey<T>,
) : TypeSerializer2<List<T>> { // 修改泛型为 List<T>

    final override fun deserialize(type: Type, node: ConfigurationNode): List<T> {
        return node.getList<String>(emptyList())
            .flatMap { parseEntry(it) }
            .distinct()
    }

    private fun parseEntry(entry: String): List<T> = when {
        entry.startsWith("#") -> resolveTag(entry)
        else -> listOfNotNull(resolveDirect(entry))
    }

    private fun resolveTag(tagEntry: String): List<T> {
        val tagName = tagEntry.substringAfter('#')
        val tagKey = TagKey.create(registryKey, Identifier.key(tagName))
        val registry = getRegistry()

        return registry.getTag(tagKey)
            ?.resolve(registry)
            ?.toList()
            .applyIfNull { logTagError(tagName) }
            .orEmpty()
    }

    private fun resolveDirect(entry: String): T? {
        return getRegistry().get(Identifier.key(entry)).applyIfNull { logEntryError(entry) }
    }

    private fun getRegistry(): Registry<T> = when (registryKey) {
        RegistryKey.BLOCK -> Registry.BLOCK as Registry<T>
        RegistryKey.ITEM -> Registry.ITEM as Registry<T>
        else -> throw IllegalArgumentException("Unsupported registry: $registryKey")
    }

    private fun logTagError(tagName: String) {
        LOGGER.error("Unknown ${registryType()} tag: #$tagName")
    }

    private fun logEntryError(entry: String) {
        LOGGER.error("Unknown ${registryType()} entry: $entry")
    }

    private fun registryType(): String {
        return registryKey.key().asString().substringAfter(':')
    }

    override fun serialize(type: Type, obj: List<T>?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}
