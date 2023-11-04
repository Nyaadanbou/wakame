package cc.mewcraft.wakame.item

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

/**
 * Represents an item template.
 */
class WakaItem(
    val key: Key,
) {
    val attributeContainers: Map<String, Slot> = HashMap()

    fun createItemStack(player: Player?) {
        TODO()
    }
}