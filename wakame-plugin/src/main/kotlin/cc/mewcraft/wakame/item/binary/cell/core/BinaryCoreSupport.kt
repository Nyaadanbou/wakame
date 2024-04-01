package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * By design, an empty [BinaryCore] is a special core in which the player
 * can replace it with something else. See the "reforge" module for more
 * details.
 */
data object EmptyBinaryCore : BinaryCore {
    override val key: Nothing get() = throw UnsupportedOperationException("EmptyBinaryCore has no key")
    override fun asShadowTag(): ShadowTag = me.lucko.helper.shadows.nbt.CompoundShadowTag.create()
}

/**
 * A binary core of an ability.
 *
 * @property key the key of the ability
 */
data class BinaryAbilityCore(
    override val key: Key,
) : BinaryCore {
    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putString(NekoTags.Cell.CORE_KEY, key.asString())
    }
}

/**
 * A binary core of an attribute.
 *
 * @property key the key of the attribute
 */
data class BinaryAttributeCore(
    override val key: Key,
    val data: BinaryAttributeData,
) : BinaryCore, AttributeModifierProvider {
    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        val attributeModifierFactory = AttributeRegistry.FACADES[key].MODIFIER_FACTORY
        val attributeModifiers = attributeModifierFactory.createAttributeModifiers(uuid, data)
        return attributeModifiers
    }

    override fun asShadowTag(): ShadowTag {
        val nbtDecoder = AttributeRegistry.FACADES[key].BINARY_DATA_NBT_DECODER
        val nbtData = nbtDecoder.decode(data)
        return nbtData
    }
}