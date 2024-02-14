package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValue
import cc.mewcraft.wakame.util.getOrThrow
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

data class BinaryAttributeCore(
    override val key: Key,
    override val value: BinaryAttributeValue,
) : BinaryCore, AttributeModifierProvider {

    @OptIn(InternalApi::class)
    override fun provideAttributeModifiers(uuid: UUID): Map<out Attribute, AttributeModifier> {
        val factory = AttributeRegistry.attributeFactoryRegistry.getOrThrow(key)
        val modifiers = factory.createAttributeModifiers(uuid, value)
        return modifiers
    }

    @OptIn(InternalApi::class)
    override fun asShadowTag(): ShadowTag {
        val encoder = AttributeRegistry.shadowTagEncoder.getOrThrow(key)
        val tag = encoder.encode(value)
        return tag
    }
}