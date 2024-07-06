package cc.mewcraft.wakame.tick

fun interface Tickable {
    /**
     * 执行一次 Tick 在此 [Tickable].
     *
     * @return [TickResult] 表示此次 Tick 的结果.
     * @see TickResult
     */
    fun tick(): TickResult
}