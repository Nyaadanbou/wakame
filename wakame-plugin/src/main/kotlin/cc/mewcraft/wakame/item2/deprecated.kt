package cc.mewcraft.wakame.item2

import org.bukkit.entity.Player
import java.util.*

object ItemDamageEventMarker {
    private val markers: MutableSet<UUID> = mutableSetOf()

    fun markAlreadyDamaged(player: Player) {
        markers.add(player.uniqueId)
    }

    fun isAlreadyDamaged(player: Player): Boolean {
        return markers.remove(player.uniqueId)
    }
}