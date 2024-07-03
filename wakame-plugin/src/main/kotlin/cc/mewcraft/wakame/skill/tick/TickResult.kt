package cc.mewcraft.wakame.skill.tick

enum class TickResult {
    /**
     * 技能 Tick 完成, 继续执行下一个 Tick.
     */
    CONTINUE_TICK,

    /**
     * 技能**整个执行**完成.
     */
    ALL_DONE,

    /**
     * 技能出现错误, 中断技能执行.
     */
    INTERRUPT,
    ;
}