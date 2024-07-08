package cc.mewcraft.wakame.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import me.lucko.helper.terminable.Terminable
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * 事件总线类, 用于在不同的组件之间传递事件.
 *
 * @param scope 用于管理协程的作用域.
 */
class EventBus(
    private val scope: CoroutineScope,
) : Terminable {
    private val eventChannel = Channel<Any>()

    /**
     * 发布事件.
     *
     * @param event 要发布的事件.
     */
    suspend fun post(event: Any) {
        eventChannel.send(event)
    }

    /**
     * 订阅特定类型的事件.
     *
     * @param T 要订阅的事件类型.
     * @param eventType 要订阅的事件类型的 KClass 对象.
     * @param handler 处理订阅事件的挂起函数.
     * @return 管理订阅事件处理的 Job 对象.
     */
    fun <T : Any> subscribe(eventType: KClass<T>, handler: suspend (T) -> Unit): Job {
        return scope.launch {
            for (event in eventChannel) {
                if (eventType.isInstance(event)) {
                    try {
                        handler(eventType.cast(event))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * 关闭事件总线, 取消所有协程.
     */
    override fun close() {
        eventChannel.close()
        scope.cancel()
    }
}

/**
 * 内联函数, 订阅特定类型的事件.
 *
 * @param T 要订阅的事件类型.
 * @param handler 处理订阅事件的挂起函数.
 * @return 管理订阅事件处理的 Job 对象.
 */
inline fun <reified T : Any> EventBus.subscribe(noinline handler: suspend (T) -> Unit): Job {
    return this.subscribe(T::class, handler)
}
