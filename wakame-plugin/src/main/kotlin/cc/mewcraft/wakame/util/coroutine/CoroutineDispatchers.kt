package cc.mewcraft.wakame.util.coroutine

import cc.mewcraft.wakame.Koish
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * Minecraft async dispatcher.
 */
val Dispatchers.async: CoroutineContext
    get() = DispatcherContainer.async

/**
 * Minecraft sync dispatcher.
 */
val Dispatchers.minecraft: CoroutineContext
    get() = DispatcherContainer.sync

/**
 * Container for the dispatchers.
 */
object DispatcherContainer {
    private var asyncCoroutine: CoroutineContext? = null
    private var syncCoroutine: CoroutineContext? = null

    /**
     * Gets the async coroutine context.
     */
    val async: CoroutineContext
        get() {
            if (asyncCoroutine == null) {
                asyncCoroutine = AsyncCoroutineDispatcher(Koish)
            }

            return asyncCoroutine!!
        }

    /**
     * Gets the sync coroutine context.
     */
    val sync: CoroutineContext
        get() {
            if (syncCoroutine == null) {
                syncCoroutine = MinecraftCoroutineDispatcher(Koish)
            }

            return syncCoroutine!!
        }
}

class MinecraftCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            plugin.server.scheduler.runTask(plugin, block)
        }
    }
}

class AsyncCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            plugin.server.scheduler.runTaskAsynchronously(plugin, block)
        } else {
            block.run()
        }
    }
}
