package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.ability.AbilityBinaryValue
import cc.mewcraft.wakame.ability.AbilityCoreCodec
import cc.mewcraft.wakame.ability.AbilityCoreCodecRegistry
import cc.mewcraft.wakame.ability.AbilitySchemeValue
import net.kyori.adventure.key.Key

data class SchemeAbilityCore(
    override val key: Key,
    override val value: AbilitySchemeValue,
) : SchemeCore {

    /**
     * Gets a [AbilityBinaryValue] generated from the [value] and the given
     * [scalingFactor].
     *
     * Note that the returned value entirely depends on the [value] and the
     * [scalingFactor]. Even if the given [scalingFactor] is the same, each
     * call of this function may return a different value due to the fact
     * that [AbilityBinaryValue] may produce a different result on each call.
     *
     * @param scalingFactor the scaling factor, such as item levels
     * @return a new random [AbilityBinaryValue]
     */
    override fun generate(scalingFactor: Int): AbilityBinaryValue {
        // 根据设计，技能的数值分为两类：
        // 1 技能本身的数值
        // 2 技能依赖的外部数值
        // 技能本身的数值，写死在配置文件里，不太需要动态变化，顶多用到随机数值
        // 技能依赖的外部数值，目前包括属性数值，触发时才会知道。这部分取决于技能的具体实现
        val codec = AbilityCoreCodecRegistry.getOrThrow<AbilityCoreCodec<AbilityBinaryValue, AbilitySchemeValue>>(key)
        val value = codec.generate(value, scalingFactor)
        return value
    }
}