package cc.mewcraft.wakame.util.eventbus

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 事件总线, 使用 [ConcurrentHashMap] 实现.
 */
object MapEventBus {

    private val subscribers = ConcurrentHashMap<KClass<*>, ArrayList<(Any) -> Unit>>()

    /**
     * 订阅指定类型的事件.
     */
    fun <T : Any> subscribe(eventType: KClass<T>, listener: (T) -> Unit) {
        subscribers.computeIfAbsent(eventType) { ArrayList() }.add { event -> listener(event as T) }
    }

    /**
     * 取消订阅指定类型的事件.
     */
    fun <T : Any> unsubscribe(eventType: KClass<T>, listener: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subscribers[eventType]?.remove(listener as (Any) -> Unit)
    }

    /**
     * 扩展函数: 便捷注册.
     */
    inline fun <reified T : Any> subscribe(noinline listener: (T) -> Unit) = subscribe(T::class, listener)

    /**
     * 扩展函数: 便捷取消订阅.
     */
    inline fun <reified T : Any> unsubscribe(noinline listener: (T) -> Unit) = unsubscribe(T::class, listener)

    /**
     * 发布一个事件, 通知所有监听器.
     */
    fun post(event: Any) {
        subscribers[event::class]?.forEach { it(event) }
    }

    /**
     * 清空所有订阅的事件.
     */
    fun clear() {
        subscribers.clear()
    }

    /**
     * 是否存在监听器.
     */
    fun isListened(eventType: KClass<*>): Boolean {
        return !subscribers[eventType].isNullOrEmpty()
    }
}
