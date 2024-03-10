package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.ability.PlainAbilityData
import cc.mewcraft.wakame.ability.SchemaAbilityData
import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.util.getOrThrow
import net.kyori.adventure.key.Key

data class SchemeAbilityCore(
    override val key: Key,
    override val value: SchemaAbilityData,
) : SchemeCore {

    /**
     * Gets a [PlainAbilityData] generated from the [value] and the given
     * [context].
     *
     * Note that the returned value entirely depends on the [value] and
     * the [context]. Even if the given [context] is the same, each call
     * of this function may return a different value due to the fact that
     * [PlainAbilityData] may produce a different result on each call.
     *
     * @param context the context
     * @return a new instance
     */
    override fun generate(context: SchemeGenerationContext): BinaryCoreData {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值
        // 技能本身的数值，写死在配置文件里，不太需要动态变化，顶多用到随机数值
        // 技能依赖的外部数值，目前包括属性数值，技能触发时才会知道

        val baker = AbilityRegistry.schemaCoreDataBaker.getOrThrow(key)
        val factor = context.level
        val ret = baker.bake(value, factor)
        return ret
    }
}