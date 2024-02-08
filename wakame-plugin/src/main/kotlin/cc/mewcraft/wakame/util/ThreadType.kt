package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.*

enum class ThreadType {
    SYNC,
    ASYNC,
    DISPATCHERS_ASYNC,
    REMAIN,
    ;

    suspend fun <T> switchContext(block: suspend () -> T): T {
        if (!NEKO_PLUGIN.isEnabled) {
            return block()
        }
        if (this == REMAIN) {
            return block()
        }

        return withContext(
            when (this) {
                SYNC -> NEKO_PLUGIN.minecraftDispatcher
                ASYNC -> NEKO_PLUGIN.minecraftDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO + CoroutineName("Neko IO")
                else -> throw IllegalStateException("Unknown thread type: $this")
            }
        ) {
            block()
        }
    }

    fun launch(block: suspend () -> Unit): Job {
        if (!NEKO_PLUGIN.isEnabled) {
            runBlocking {
                block()
            }
            return Job()
        }
        if (this == REMAIN) {
            return NEKO_PLUGIN.launch {
                block()
            }
        }

        return NEKO_PLUGIN.launch(
            when (this) {
                SYNC -> NEKO_PLUGIN.minecraftDispatcher
                ASYNC -> NEKO_PLUGIN.minecraftDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO + CoroutineName("Neko IO")
                else -> throw IllegalStateException("Unknown thread type: $this")
            }
        ) {
            block()
        }
    }
}