package cc.mewcraft.wakame.user

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap

/**
 * The User Manager on Paper platform.
 */
class PaperUserListener : UserListener, KoinComponent {
    private val saveLoadExecutors: MutableSet<SaveLoadExecutor> = ConcurrentHashMap.newKeySet()

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        for (executor in saveLoadExecutors) {
            executor.loadFrom(player.persistentDataContainer)
        }
    }

    @EventHandler
    private fun onJoin(e: PlayerJoinEvent) {
        // create user data for the player
        val player = e.player

        for (executor in saveLoadExecutors) {
            executor.saveTo(player.persistentDataContainer)
        }
    }

    override fun registerUserPersistentDataAccessor(executor: SaveLoadExecutor) {
        saveLoadExecutors.add(executor)
    }

    override fun unregisterUserPersistentDataAccessor(executor: SaveLoadExecutor) {
        saveLoadExecutors.remove(executor)
    }
}