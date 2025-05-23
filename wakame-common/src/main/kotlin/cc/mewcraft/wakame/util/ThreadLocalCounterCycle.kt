package cc.mewcraft.wakame.util

/**
 * 一个线程安全的计数器, 用于循环计数.
 */
class ThreadLocalCounterCycle(
    private val limit: Int,
) {
    private companion object {
        val threadLocalCounter: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }
    }

    /**
     * 获取下一个计数值.
     */
    fun next(): Int {
        val current = threadLocalCounter.get()
        threadLocalCounter.set((current + 1) % limit)
        return current
    }
}