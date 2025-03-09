package cc.mewcraft.wakame.ability.data

enum class StatePhase {
    IDLE,
    CAST_POINT,
    CASTING,
    BACKSWING,
    RESET,
    ;

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