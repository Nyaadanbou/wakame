package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO
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
        if (!NEKO.isEnabled) {
            return block()
        }
        if (this == REMAIN) {
            return block()
        }

        return withContext(
            when (this) {
                SYNC -> NEKO.minecraftDispatcher
                ASYNC -> NEKO.asyncDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
                else -> throw IllegalStateException("Unknown thread type: $this")
            }
        ) {
            block()
        }
    }

    fun launch(block: suspend () -> Unit): Job {
        if (!NEKO.isEnabled) {
            runBlocking {
                block()
            }
            return Job()
        }

        return NEKO.launch(
            when (this) {
                SYNC -> NEKO.minecraftDispatcher
                ASYNC -> NEKO.asyncDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
                REMAIN -> if (server.isPrimaryThread) NEKO.minecraftDispatcher else NEKO.asyncDispatcher
            }
        ) {
            block()
        }
    }
}