package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.SchemeCoreValue

/**
 * 代表一个技能在模板中的数据。
 */
sealed interface SchemeAbilityValue : SchemeCoreValue

data class SchemeAbilityValueMap(
    val map: MutableMap<String, Any>,
) : SchemeAbilityValue {
    override fun realize(factor: Number): BinaryCoreValue {
        TODO("Not yet implemented")
    }
}