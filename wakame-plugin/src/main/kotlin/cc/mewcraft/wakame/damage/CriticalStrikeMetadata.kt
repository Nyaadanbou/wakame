package cc.mewcraft.wakame.damage

import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 伤害的暴击元数据.
 */
data class CriticalStrikeMetadata(
    /**
     * 暴击倍率的值.
     */
    val power: Double,

    /**
     * 这次伤害的暴击状态.
     */
    val state: CriticalStrikeState,
) {
    companion object {
        /**
         * 默认的暴击元数据.
         * 用于不会暴击的攻击.
         */
        val DEFAULT: CriticalStrikeMetadata = CriticalStrikeMetadata(1.0, CriticalStrikeState.NONE)

        /**
         * 通过属性计算和随机得到暴击元数据.
         */
        fun byCalculate(chance: Double, positivePower: Double, negativePower: Double, nonePower: Double): CriticalStrikeMetadata {
            val power: Double
            val state: CriticalStrikeState
            if (chance < 0) {
                if (Random.nextDouble() < chance.absoluteValue) {
                    state = CriticalStrikeState.NEGATIVE
                    power = negativePower
                } else {
                    state = CriticalStrikeState.NONE
                    power = nonePower
                }
            } else {
                if (Random.nextDouble() < chance) {
                    state = CriticalStrikeState.POSITIVE
                    power = positivePower
                } else {
                    state = CriticalStrikeState.NONE
                    power = nonePower
                }
            }
            return CriticalStrikeMetadata(power, state)
        }

    }
}

/**
 * 暴击状态.
 */
enum class CriticalStrikeState {
    /**
     * 正暴击.
     */
    POSITIVE,

    /**
     * 负暴击.
     */
    NEGATIVE,

    /**
     * 无暴击.
     */
    NONE
}