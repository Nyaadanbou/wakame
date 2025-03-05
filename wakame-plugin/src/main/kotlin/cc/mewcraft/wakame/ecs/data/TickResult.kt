package cc.mewcraft.wakame.ecs.data

/**
 * 代表一次 tick 的执行结果.
 */
enum class TickResult {
    /**
     * 此 Tick 完成, 会继续执行下一个 Tick.
     */
    CONTINUE_TICK,

    /**
     * 此 Tick 完成, 并且可以进行到下一个 [cc.mewcraft.wakame.ecs.data.StatePhase].
     */
    NEXT_STATE,

    /**
     * 此 Tick 完成, 并且可以进行到下一个 [cc.mewcraft.wakame.ecs.data.StatePhase].
     * 但不会触发消耗. 适合条件不满足时的 TickResult.
     */
    NEXT_STATE_NO_CONSUME,

    /**
     * 重置状态, 会回到 [cc.mewcraft.wakame.ecs.data.StatePhase.IDLE] 状态.
     */
    RESET_STATE,

    /**
     * 此 Tick 执行完成, 不再执行下一个 Tick. (即从 ECS 系统中移除)
     */
    ALL_DONE,

    /**
     * 出现错误, 中断执行.
     */
    INTERRUPT,
    ;

    fun isNextState(): Boolean = this == NEXT_STATE || this == NEXT_STATE_NO_CONSUME
    fun isDone(): Boolean = this == ALL_DONE || this == INTERRUPT
}