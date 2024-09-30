package cc.mewcraft.wakame.eventbus

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * 事件总线类, 用于在不同的组件之间传递事件.
 *
 * ## 警告!!!
 * 该实现不保证在并发环境下正常运作!
 * 所有涉及到的协程只能由单个线程运行.
 *
 * @param scope 用于管理协程的作用域.
 */
class EventBus(
    private val scope: CoroutineScope,
) {
    // 必须使用 MutableSharedFlow, 这样发布的事件才可以被多次消费
    private val eventFlow = MutableSharedFlow<Any>(replay = 1)

    /**
     * 发布事件.
     *
     * @param event 要发布的事件.
     */
    suspend fun post(event: Any) {
        eventFlow.emit(event)
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
        return eventFlow.onEach {
            if (eventType.isInstance(it)) {
                try {
                    handler(eventType.cast(it))
                } catch (_: CancellationException) {
                    println("The subscription has been cancelled")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }.launchIn(scope)
    }

    /**
     * 关闭事件总线, 取消所有等待中的协程.
     */
    fun close() {
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
