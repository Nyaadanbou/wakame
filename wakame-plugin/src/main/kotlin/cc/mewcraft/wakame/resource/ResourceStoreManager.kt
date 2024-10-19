package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.user.SaveLoadExecutor
import cc.mewcraft.wakame.user.UserListener
import cc.mewcraft.wakame.user.UserManager
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ResourceStoreManager : Initializable, KoinComponent {
    private val userListener: UserListener by inject()

    override fun onPreWorld() {
        userListener.registerSaveLoadExecutor(HealthSaveLoadExecutor)
        userListener.registerSaveLoadExecutor(ManaSaveLoadExecutor)
    }

    override fun close() {
        userListener.unregisterSaveLoadExecutor(HealthSaveLoadExecutor)
        userListener.unregisterSaveLoadExecutor(ManaSaveLoadExecutor)
    }
}

private object HealthSaveLoadExecutor : SaveLoadExecutor {
    private val HEALTH_KEY = NamespacedKey("wakame", "player_health")

    override fun saveTo(player: Player) {
        val health = player.health
        val pdc = player.persistentDataContainer
        pdc.set(HEALTH_KEY, PersistentDataType.DOUBLE, health)
    }

    override fun loadFrom(player: Player) {
        val pdc = player.persistentDataContainer
        val health = pdc.get(HEALTH_KEY, PersistentDataType.DOUBLE)
        if (health != null) {
            player.health = health
        }
    }
}

private object ManaSaveLoadExecutor : SaveLoadExecutor, KoinComponent {
    private val userManager: UserManager<Player> by inject()

    private val MANA_KEY = NamespacedKey("wakame", "player_mana")

    override fun saveTo(player: Player) {
        val user = userManager.getPlayer(player)
        val mana = user.resourceMap.current(ResourceTypeRegistry.MANA)
        val pdc = player.persistentDataContainer
        pdc.set(MANA_KEY, PersistentDataType.INTEGER, mana)
    }

    override fun loadFrom(player: Player) {
        val pdc = player.persistentDataContainer
        val mana = pdc.get(MANA_KEY, PersistentDataType.INTEGER)
        if (mana != null) {
            val user = userManager.getPlayer(player)
            user.resourceMap.set(ResourceTypeRegistry.MANA, mana)
        }
    }
}