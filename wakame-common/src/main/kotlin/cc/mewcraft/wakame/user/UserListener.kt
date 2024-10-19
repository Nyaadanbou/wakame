package cc.mewcraft.wakame.user

import org.bukkit.event.Listener

interface UserListener : Listener {
    /**
     * Register the [SaveLoadExecutor] to the [UserListener].
     */
    fun registerSaveLoadExecutor(executor: SaveLoadExecutor)

    /**
     * Unregister the [SaveLoadExecutor] from the [UserListener].
     */
    fun unregisterSaveLoadExecutor(executor: SaveLoadExecutor)
}