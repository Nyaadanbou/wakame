package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.ability.AbilityBinaryValue
import cc.mewcraft.wakame.ability.AbilityCoreCodec
import cc.mewcraft.wakame.ability.AbilityCoreCodecRegistry
import cc.mewcraft.wakame.ability.AbilitySchemeValue
import cc.mewcraft.wakame.attribute.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.SchemeAttributeValue
import cc.mewcraft.wakame.attribute.instance.AttributeCoreCodec
import cc.mewcraft.wakame.attribute.instance.AttributeCoreCodecRegistry
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.binary.cell.CellTagNames
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.core.SchemeAbilityCore
import cc.mewcraft.wakame.item.scheme.core.SchemeAttributeCore
import cc.mewcraft.wakame.item.scheme.core.EmptySchemeCore
import cc.mewcraft.wakame.item.scheme.core.SchemeCore
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * A factory used to create [BinaryCore] from scheme and binary sources.
 */
object BinaryCoreFactory {
    /**
     * Creates an [BinaryCore] from a NBT source.
     *
     * @param compoundTag the tag
     * @return an [BinaryCore] or [emptyBinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun decode(compoundTag: CompoundShadowTag): BinaryCore {
        if (compoundTag.isEmpty) {
            return emptyBinaryCore()
        }

        val id = compoundTag.getString(CellTagNames.CORE_ID)
        val key = Key.key(id)

        val ret: BinaryCore
        when (key.namespace()) {
            Core.ABILITY_NAMESPACE -> {
                val codec: AbilityCoreCodec<AbilityBinaryValue, AbilitySchemeValue> = AbilityCoreCodecRegistry.getOrThrow(key)
                val value: AbilityBinaryValue = codec.decode(compoundTag)
                ret = BinaryAbilityCore(key, value)
            }

            Core.ATTRIBUTE_NAMESPACE -> {
                val codec: AttributeCoreCodec<BinaryAttributeValue, SchemeAttributeValue> = AttributeCoreCodecRegistry.getOrThrow(key)
                val value: BinaryAttributeValue = codec.decode(compoundTag)
                ret = BinaryAttributeCore(key, value)
            }

            else -> throw IllegalArgumentException("Failed to parse binary tag ${compoundTag.asString()}")
        }

        return ret
    }

    /**
     * Creates an [BinaryCore] from a scheme source.
     *
     * @param context the context
     * @param schemeCore the scheme core
     * @return a new instance
     * @throws IllegalArgumentException
     */
    fun generate(context: SchemeGenerationContext, schemeCore: SchemeCore): BinaryCore {
        val ret: BinaryCore
        when (schemeCore) {
            is EmptySchemeCore -> {
                ret = emptyBinaryCore()
            }

            is SchemeAbilityCore -> {
                val value = schemeCore.generate(context.itemLevel)
                ret = BinaryAbilityCore(schemeCore.key(), value)
            }

            is SchemeAttributeCore -> {
                val value = schemeCore.generate(context.itemLevel)
                ret = BinaryAttributeCore(schemeCore.key(), value)
            }
        }
        return ret
    }
}
