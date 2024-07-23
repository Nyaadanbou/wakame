package cc.mewcraft.wakame.tick

interface Tickable {
    var tickCount: Long

    /**
     * 执行一次 Tick 在此 [Tickable].
     *
     * @return [TickResult] 表示此次 Tick 的结果.
     * @see TickResult
     */
    fun tick(): TickResult

    fun whenRemove() {}
}

class AlwaysTickable(
    private val block: () -> Unit
): Tickable {
    override var tickCount: Long = 0

    override fun tick(): TickResult {
        block()
        return TickResult.CONTINUE_TICK
    }
}