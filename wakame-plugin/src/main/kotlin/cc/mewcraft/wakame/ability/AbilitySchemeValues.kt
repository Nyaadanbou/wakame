package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.SchemeCoreValue

/**
 * 代表一个技能在模板中的数据。
 */
sealed interface AbilitySchemeValue : SchemeCoreValue

data class AbilitySchemeValueMap(
    val map: MutableMap<String, Any>, // FIXME 参考 attributes，完成这里
) : AbilitySchemeValue