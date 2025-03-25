package cc.mewcraft.wakame.util.concurrent

import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * 提供一个线程本地的 [ObjectArrayList] 实例.
 * 该类型应该直接由单例实现, 例如 `object class` 和 `companion object`.
 *
 * @param T 元素类型
 * @property onRead 读取时的回调
 * @property onWrite 写入时的回调
 */
open class ThreadLocalListProvider<T>(
    private val onRead: ObjectArrayList<T>.() -> Unit = {},
    private val onWrite: ObjectArrayList<T>.() -> Unit = {},
) {

    private val _threadLocalList = ThreadLocal.withInitial { ObjectArrayList<T>() }

    protected var threadLocalList: ObjectArrayList<T>
        get() = _threadLocalList.get().apply(onRead)
        set(value) = _threadLocalList.set(value.apply(onWrite))

}