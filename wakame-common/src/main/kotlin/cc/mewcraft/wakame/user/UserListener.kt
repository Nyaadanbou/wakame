package cc.mewcraft.wakame.user

import org.bukkit.NamespacedKey
import org.bukkit.event.Listener

interface UserListener : Listener {
    companion object {
        val PLAYER_HEALTH: NamespacedKey = NamespacedKey("wakame", "player_health")
        val PLAYER_MANA: NamespacedKey = NamespacedKey("wakame", "player_mana")
    }
}