package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.registry.AttributeRegistry
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

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