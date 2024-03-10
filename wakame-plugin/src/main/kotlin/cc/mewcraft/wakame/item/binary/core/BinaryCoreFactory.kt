package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.facade.PlainAttributeData
import cc.mewcraft.wakame.attribute.facade.element
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.core.SchemeAbilityCore
import cc.mewcraft.wakame.item.scheme.core.SchemeAttributeCore
import cc.mewcraft.wakame.item.scheme.core.SchemeCore
import cc.mewcraft.wakame.item.scheme.filter.AbilityContextHolder
import cc.mewcraft.wakame.item.scheme.filter.AttributeContextHolder
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
     * @param compound the tag
     * @return an [BinaryCore] or [emptyBinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun decode(compound: CompoundShadowTag): BinaryCore {
        if (compound.isEmpty) {
            return emptyBinaryCore()
        }

        val key = Key.key(compound.getString(NekoTags.Cell.CORE_KEY))
        val ret = when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                BinaryAbilityCore(key)
            }

            NekoNamespaces.ATTRIBUTE -> {
                val encoder = AttributeRegistry.plainNbtEncoder.getOrThrow(key)
                val data = encoder.encode(compound)
                BinaryAttributeCore(key, data)
            }

            else -> throw IllegalArgumentException("Failed to parse binary tag ${compound.asString()}")
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
        val key = schemeCore.key
        val ret = when (schemeCore) {
            is SchemeAbilityCore -> {
                // populate context
                val contextHolder = AbilityContextHolder(key)
                context.abilities += contextHolder

                // construct core
                BinaryAbilityCore(key)
            }

            is SchemeAttributeCore -> {
                // populate context
                val attributeData = schemeCore.generate(context) as PlainAttributeData
                val contextHolder = AttributeContextHolder(key, attributeData.operation, attributeData.element)
                context.attributes += contextHolder

                // construct core
                BinaryAttributeCore(key, attributeData)
            }
        }

        return ret
    }

}
