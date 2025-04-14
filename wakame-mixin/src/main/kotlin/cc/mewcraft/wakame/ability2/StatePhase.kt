package cc.mewcraft.wakame.ability2

/**
 * 表示技能的执行状态.
 */
sealed interface StatePhase {
    val isCostMana: Boolean

    class Idle(override val isCostMana: Boolean = false) : StatePhase

    class CastPoint(override val isCostMana: Boolean = false) : StatePhase

    class Casting(override val isCostMana: Boolean = false) : StatePhase

    class Backswing(override val isCostMana: Boolean = false) : StatePhase

    class Reset(override val isCostMana: Boolean = false) : StatePhase
}
