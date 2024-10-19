package cc.mewcraft.wakame.user

import org.bukkit.event.Listener

interface UserListener : Listener {
    /**
     * Register the [SaveLoadExecutor] to the [UserListener].
     */
    fun registerUserPersistentDataAccessor(executor: SaveLoadExecutor)

    /**
     * Unregister the [SaveLoadExecutor] from the [UserListener].
     */
    fun unregisterUserPersistentDataAccessor(executor: SaveLoadExecutor)
}