package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.KOISH_SCOPE
import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.coroutine.async
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.*

enum class ThreadType {
    SYNC,
    ASYNC,
    REMAIN,
    ;

    suspend fun <T> switchContext(block: suspend () -> T): T {
        if (!Koish.isEnabled) {
            return block()
        }
        if (this == REMAIN) {
            return block()
        }

        return withContext(
            when (this) {
                SYNC -> Dispatchers.minecraft
                ASYNC -> Dispatchers.async
                else -> throw IllegalStateException("Unknown thread type: $this")
            }
        ) {
            block()
        }
    }

    fun launch(block: suspend () -> Unit): Job {
        if (!Koish.isEnabled) {
            runBlocking {
                block()
            }
            return Job()
        }

        return KOISH_SCOPE.launch(
            when (this) {
                SYNC -> Dispatchers.minecraft
                ASYNC -> Dispatchers.async
                REMAIN -> if (isServerThread) Dispatchers.minecraft else Dispatchers.async
            }
        ) {
            block()
        }
    }
}