package cc.mewcraft.wakame.util.eventbus

import cc.mewcraft.wakame.KOISH_SCOPE
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

// FIXME 未经完全测试, 请勿使用.

/**
 * 事件总线, 基于 [kotlinx.coroutines.flow.Flow] 实现.
 *
 * ### 注意事项
 * 1. 该实现无法在并发环境下正常运行.
 * 2. 该实现 ...
 */
object FlowEventBus {

    // 必须使用 MutableSharedFlow, 这样发布的事件才可以被多次消费
    private val flowEvents = ConcurrentHashMap<String, MutableSharedFlow<Any>>()

    private fun getFlow(key: String): MutableSharedFlow<Any> {
        return flowEvents.computeIfAbsent(key) { MutableSharedFlow() }
    }

    fun post(
        event: Any,
        dispatcher: CoroutineDispatcher = Dispatchers.minecraft,
        delay: Long = 0
    ) {
        KOISH_SCOPE.launch(dispatcher) {
            delay(delay)
            getFlow(event.javaClass.simpleName).emit(event)
        }
    }

    @PublishedApi
    internal fun <T : Any> subscribe(
        clazz: KClass<T>,
        dispatcher: CoroutineDispatcher = Dispatchers.minecraft,
        handler: (T) -> Unit
    ): Job = KOISH_SCOPE.launch(dispatcher) {
        getFlow(clazz.java.simpleName).collect {
            if (clazz.isInstance(it)) {
                @Suppress("UNCHECKED_CAST")
                try {
                    handler(it as T)
                } catch (e: Exception) {
                    LOGGER.error("Error while handling event ${it.javaClass.simpleName}", e)
                }
            }
        }
    }

    inline fun <reified T : Any> subscribe(
        dispatcher: CoroutineDispatcher = Dispatchers.minecraft,
        noinline handler: (T) -> Unit
    ): Job = subscribe(T::class, dispatcher, handler)

}
