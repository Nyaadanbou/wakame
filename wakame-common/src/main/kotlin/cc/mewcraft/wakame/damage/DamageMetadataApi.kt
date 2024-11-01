package cc.mewcraft.wakame.damage


/**
 * 伤害元数据, 包含了一次伤害中"攻击阶段"的有关信息.
 * 一旦实例化后, 攻击伤害的数值以及各种信息就已经确定.
 */
data class DamageMetadata(
    /**
     * 伤害标签.
     */
    val damageTags: DamageTags,

    /**
     * 伤害捆绑包.
     */
    val damageBundle: DamageBundle,

    /**
     * 暴击元数据.
     */
    val criticalStrikeMetadata: CriticalStrikeMetadata,
)

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
    companion object Constants {
        /**
         * 默认的暴击元数据.
         * 用于不会暴击的攻击.
         */
        @JvmField
        val NONE: CriticalStrikeMetadata = CriticalStrikeMetadata(1.0, CriticalStrikeState.NONE)
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
