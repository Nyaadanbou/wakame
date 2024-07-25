package cc.mewcraft.wakame.tick

interface Tickable {
    companion object {
        fun always(block: () -> Unit): Tickable {
            return Always(block)
        }
    }

    var tickCount: Long

    /**
     * 执行一次 Tick 在此 [Tickable].
     *
     * @return [TickResult] 表示此次 Tick 的结果.
     * @see TickResult
     */
    fun tick(): TickResult

    fun whenRemove() {}

    private class Always(
        private val block: () -> Unit
    ): Tickable {
        override var tickCount: Long = 0

        override fun tick(): TickResult {
            block()
            return TickResult.CONTINUE_TICK
        }
    }
}