package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.KOISH_SCOPE
import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.coroutine.async
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.*

// 截止 2025/1/27, 如果要在任意地方启动一个协程, 比较好的做法
// 应该是使用 KOISH_SCOPE#launch 来启动一个协程, 而不是
// 使用这里的 ThreadType.

enum class ThreadType {
    SYNC,
    ASYNC,
    REMAIN,
    ;

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        if (!Koish.isEnabled) {
            runBlocking { block() }
            return Job()
        }

        return KOISH_SCOPE.launch(
            context = when (this) {
                SYNC -> Dispatchers.minecraft
                ASYNC -> Dispatchers.async
                REMAIN -> if (isServerThread) Dispatchers.minecraft else Dispatchers.async
            },
            block = block
        )
    }
}