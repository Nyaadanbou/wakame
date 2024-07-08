package cc.mewcraft.wakame.eventbus

import me.lucko.helper.terminable.Terminable

/**
 * 订阅者对象, 提供注册订阅的方法.
 */
object Subscribers {

    /**
     * 注册一个订阅, 并返回一个 Terminable 用于取消订阅.
     *
     * @param T 要订阅的事件类型.
     * @param bus 事件总线对象.
     * @param subscriber 处理订阅事件的挂起函数.
     * @return Terminable 对象, 调用它的 close 方法可以取消订阅.
     */
    inline fun <reified T : Any> register(bus: EventBus, noinline subscriber: suspend (T) -> Unit): Terminable {
        val job = bus.subscribe(T::class, subscriber)
        return Terminable { job.cancel() }
    }
}