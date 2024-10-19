package cc.mewcraft.wakame.compatibility.husksync

import cc.mewcraft.wakame.user.SaveLoadExecutor
import cc.mewcraft.wakame.user.UserListener
import net.william278.husksync.event.BukkitDataSaveEvent
import net.william278.husksync.event.BukkitSyncCompleteEvent
import net.william278.husksync.user.BukkitUser
import org.bukkit.event.EventHandler
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap

class HuskSyncUserListener : UserListener, KoinComponent {
    private val saveLoadExecutors: MutableSet<SaveLoadExecutor> = ConcurrentHashMap.newKeySet()

    @EventHandler
    private fun onDataSave(e: BukkitDataSaveEvent) {
        val player = (e.user as? BukkitUser)?.player ?: return
        for (executor in saveLoadExecutors) {
            executor.saveTo(player)
        }
    }

    @EventHandler
    private fun onSyncComplete(e: BukkitSyncCompleteEvent) {
        // create user data for the player
        val player = (e.user as? BukkitUser)?.player ?: return

        for (executor in saveLoadExecutors) {
            executor.loadFrom(player)
        }
    }

    override fun registerSaveLoadExecutor(executor: SaveLoadExecutor) {
        saveLoadExecutors.add(executor)
    }

    override fun unregisterSaveLoadExecutor(executor: SaveLoadExecutor) {
        saveLoadExecutors.remove(executor)
    }
}