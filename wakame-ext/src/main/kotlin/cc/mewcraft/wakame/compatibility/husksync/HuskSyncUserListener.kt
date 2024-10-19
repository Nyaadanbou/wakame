package cc.mewcraft.wakame.compatibility.husksync

import cc.mewcraft.wakame.user.UserListener
import net.william278.husksync.event.BukkitDataSaveEvent
import net.william278.husksync.event.BukkitSyncCompleteEvent
import net.william278.husksync.user.BukkitUser
import org.bukkit.event.EventHandler
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HuskSyncUserListener : UserListener, KoinComponent {
    private val userManager: UserManager by inject()
    @EventHandler
    private fun onDataSave(e: BukkitDataSaveEvent) {
        val player = (e.user as? BukkitUser)?.player ?: return
        // cleanup user data for the player
        val user = getPlayer(player)
        player.persistentDataContainer.set(UserListener.PLAYER_HEALTH, PersistentDataType.DOUBLE, player.health)
        player.persistentDataContainer.set(UserListener.PLAYER_MANA, PersistentDataType.INTEGER, user.resourceMap.current(ResourceTypeRegistry.MANA))

        user.skillMap.clear()
        userRepository.invalidate(player)
    }

    @EventHandler
    private fun onSyncComplete(e: BukkitSyncCompleteEvent) {
        // create user data for the player
        val user = getPlayer(e.player)

        val health = e.player.persistentDataContainer.get(UserListener.PLAYER_HEALTH, PersistentDataType.DOUBLE)
        if (health != null) {
            e.player.health = health
        }
        val mana = e.player.persistentDataContainer.get(UserListener.PLAYER_MANA, PersistentDataType.INTEGER)
        if (mana != null) {
            user.resourceMap.set(ResourceTypeRegistry.MANA, mana)
        }
    }
}