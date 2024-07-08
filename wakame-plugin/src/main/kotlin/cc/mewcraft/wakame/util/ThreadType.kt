package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class ThreadType : KoinComponent {
    SYNC,
    ASYNC,
    DISPATCHERS_ASYNC,
    REMAIN,
    ;

    private val server: Server by inject()

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
                ASYNC -> NEKO_PLUGIN.asyncDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
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

        return NEKO_PLUGIN.launch(
            when (this) {
                SYNC -> NEKO_PLUGIN.minecraftDispatcher
                ASYNC -> NEKO_PLUGIN.minecraftDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
                REMAIN -> if (server.isPrimaryThread) NEKO_PLUGIN.minecraftDispatcher else NEKO_PLUGIN.asyncDispatcher
            }
        ) {
            block()
        }
    }
}