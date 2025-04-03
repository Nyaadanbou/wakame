package cc.mewcraft.wakame.ability2

/**
 * 表示技能的执行状态.
 */
enum class StatePhase {
    /**
     * 闲置.
     */
    IDLE,

    /**
     * 施法前摇.
     */
    CAST_POINT,

    /**
     * 施法中.
     */
    CASTING,

    /**
     * 施法后摇.
     */
    BACKSWING,

    /**
     * 技能状态重置.
     */
    RESET,
    ;

    /**
     * 获取当前状态的下一个状态.
     */
    fun next(): StatePhase {
        return when (this) {
            IDLE -> CAST_POINT
            CAST_POINT -> CASTING
            CASTING -> BACKSWING
            BACKSWING -> RESET
            RESET -> IDLE
        }
    }
}