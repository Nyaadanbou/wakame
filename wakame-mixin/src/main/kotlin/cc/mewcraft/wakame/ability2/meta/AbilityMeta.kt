package cc.mewcraft.wakame.ability2.meta

/**
 * 代表一个已经填充了参数的技能类型.
 * 
 * @see AbilityMetaType
 */
data class AbilityMeta(
    val dataConfig: AbilityMetaContainer,
) {
    override fun toString(): String {
        return "AbilityMeta(dataConfig=$dataConfig)"
    }
}