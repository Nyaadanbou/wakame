package cc.mewcraft.wakame.ability2.data

/**
 * 代表一次 tick 的执行结果.
 */
enum class TickResult {
    /**
     * 此 Tick 完成, 会继续执行下一个 Tick.
     */
    CONTINUE_TICK,

    /**
     * 此 Tick 完成, 并且可以进行到下一个 [StatePhase].
     */
    ADVANCE_TO_NEXT_STATE,

    /**
     * 此 Tick 完成, 并且可以进行到下一个 [StatePhase],
     * 但不会触发消耗. 适合条件不满足时的 [TickResult].
     */
    ADVANCE_TO_NEXT_STATE_NO_CONSUME,

    /**
     * 重置状态, 会回到 [StatePhase.IDLE] 状态.
     */
    RESET_STATE,

    /**
     * 出现错误, 中断执行.
     */
    INTERRUPT,
    ;

    fun canAdvanceToNextState(): Boolean {
        return this == ADVANCE_TO_NEXT_STATE || this == ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }
}