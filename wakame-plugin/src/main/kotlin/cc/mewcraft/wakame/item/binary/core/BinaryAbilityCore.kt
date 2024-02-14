package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.ability.BinaryAbilityValue
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.util.getOrThrow
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

data class BinaryAbilityCore(
    override val key: Key,
    override val value: BinaryAbilityValue,
) : BinaryCore {

    @OptIn(InternalApi::class)
    override fun asShadowTag(): ShadowTag {
        val encoder = AbilityRegistry.shadowTagEncoder.getOrThrow(key)
        val compound = encoder.encode(value)
        return compound
    }
}