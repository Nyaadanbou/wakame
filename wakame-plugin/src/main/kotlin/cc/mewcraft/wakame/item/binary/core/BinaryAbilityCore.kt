package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.ability.AbilityBinaryValue
import cc.mewcraft.wakame.ability.AbilityCoreCodec
import cc.mewcraft.wakame.ability.AbilityCoreCodecRegistry
import cc.mewcraft.wakame.ability.AbilitySchemeValue
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

data class BinaryAbilityCore(
    override val key: Key,
    override val value: AbilityBinaryValue,
) : BinaryCore {

    override fun asShadowTag(): ShadowTag {
        val codec: AbilityCoreCodec<AbilityBinaryValue, AbilitySchemeValue> = AbilityCoreCodecRegistry.getOrThrow(key)
        val compound: CompoundShadowTag = codec.encode(value)
        return compound
    }
}