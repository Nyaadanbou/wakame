package cc.mewcraft.wakame.ecs.data

enum class StatePhase {
    IDLE,
    CAST_POINT,
    CASTING,
    BACKSWING,
    ;

    fun next(): StatePhase {
        return when (this) {
            IDLE -> CAST_POINT
            CAST_POINT -> CASTING
            CASTING -> BACKSWING
            BACKSWING -> IDLE // 回到初始状态，形成循环
        }
    }
}