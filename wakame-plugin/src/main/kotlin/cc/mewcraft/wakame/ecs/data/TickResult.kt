package cc.mewcraft.wakame.ecs.data

/**
 * 代表 [cc.mewcraft.wakame.ecs.Result] 的执行结果.
 */
enum class TickResult {
    /**
     * 此 Tick 完成, 会继续执行下一个 Tick.
     */
    CONTINUE_TICK,

    /**
     * 此 Tick **整个执行**完成, 不再执行下一个 Tick, 并推到下个阶段.
     */
    ALL_DONE,

    /**
     * 出现错误, 中断执行.
     */
    INTERRUPT,
    ;
}