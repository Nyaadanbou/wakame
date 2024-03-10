package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.PlainAttributeData
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.getOrThrow
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

data class BinaryAttributeCore(
    override val key: Key,
    val data: PlainAttributeData,
) : BinaryCore, AttributeModifierProvider {

    override fun provideAttributeModifiers(uuid: UUID): Map<out Attribute, AttributeModifier> {
        val factory = AttributeRegistry.attributeModifierFactory.getOrThrow(key)
        val modifiers = factory.createAttributeModifiers(uuid, data)
        return modifiers
    }

    override fun asShadowTag(): ShadowTag {
        val decoder = AttributeRegistry.plainNbtDecoder.getOrThrow(key)
        val nbt = decoder.decode(data)
        return nbt
    }

}