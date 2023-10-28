package cc.mewcraft.wakame.item

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

/**
 * Represents an item template.
 */
class WakaItem(
    val key: Key,
) {
    val attributeContainers: Map<String, AttributeContainer<*>> = HashMap()

    fun createItemStack(player: Player?) {
        TODO()
    }
}