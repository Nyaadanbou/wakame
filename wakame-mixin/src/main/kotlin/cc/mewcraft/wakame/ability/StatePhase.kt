package cc.mewcraft.wakame.ability

/**
 * 表示技能的执行状态.
 */
sealed interface StatePhase {
    val isCostMana: Boolean

    fun setCostMana(isCostMana: Boolean): StatePhase = when (this) {
        is Idle -> Idle(isCostMana)
        is CastPoint -> CastPoint(isCostMana)
        is Casting -> Casting(isCostMana)
        is Backswing -> Backswing(isCostMana)
        is Reset -> Reset(isCostMana)
    }

    class Idle(override val isCostMana: Boolean = false) : StatePhase

    class CastPoint(override val isCostMana: Boolean = false) : StatePhase

    class Casting(override val isCostMana: Boolean = false) : StatePhase

    class Backswing(override val isCostMana: Boolean = false) : StatePhase

    class Reset(override val isCostMana: Boolean = false) : StatePhase
}
