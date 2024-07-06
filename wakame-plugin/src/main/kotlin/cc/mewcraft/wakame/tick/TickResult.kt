package cc.mewcraft.wakame.tick

/**
 * 代表 [Tickable] 的执行结果.
 */
enum class TickResult {
    /**
     * Tick 完成, 会继续执行下一个 Tick.
     */
    CONTINUE_TICK,

    /**
     * 此 Tick **整个执行**完成, 不再执行下一个 Tick.
     */
    ALL_DONE,

    /**
     * 出现错误, 中断执行.
     */
    INTERRUPT,
    ;
}