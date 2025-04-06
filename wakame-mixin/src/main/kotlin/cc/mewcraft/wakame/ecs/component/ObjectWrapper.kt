package cc.mewcraft.wakame.ecs.component

abstract class ObjectWrapper<T>(
    private val delegate: T,
) {
    /**
     * 解包封装的对象.
     */
    fun unwrap(): T = delegate
}