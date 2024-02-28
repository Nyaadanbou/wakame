package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValue
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.core.SchemeAbilityCore
import cc.mewcraft.wakame.item.scheme.core.SchemeAttributeCore
import cc.mewcraft.wakame.item.scheme.core.SchemeCore
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.getOrThrow
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

        val id = compoundTag.getString(NekoTags.Cell.CORE_ID)
        val key = Key.key(id)

        val ret: BinaryCore
        when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                // val decoder = AbilityFacadeRegistry.shadowTagDecoder.getOrThrow(key)
                // val value = decoder.decode(compoundTag) // TODO finish ability facade
                ret = emptyBinaryCore()
            }

            NekoNamespaces.ATTRIBUTE -> {
                val decoder = @OptIn(InternalApi::class) AttributeRegistry.shadowTagDecoder.getOrThrow(key)
                val value = decoder.decode(compoundTag) as BinaryAttributeValue
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
            is SchemeAbilityCore -> {
                // val value = schemeCore.generate(context.itemLevel)
                // ret = BinaryAbilityCore(schemeCore.key(), value)
                ret = emptyBinaryCore() // TODO finish ability facade
            }

            is SchemeAttributeCore -> {
                val value = schemeCore.generate(context) as BinaryAttributeValue
                ret = BinaryAttributeCore(schemeCore.key(), value)
            }
        }

        return ret
    }

}
