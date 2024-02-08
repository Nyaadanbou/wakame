package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.attribute.instance.AttributeCoreCodec
import cc.mewcraft.wakame.attribute.instance.AttributeCoreCodecRegistry
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

data class BinaryAttributeCore(
    override val key: Key,
    override val value: BinaryAttributeValue,
) : BinaryCore, AttributeModifierProvider {

    override fun provideAttributeModifiers(uuid: UUID): Map<out Attribute, AttributeModifier> {
        val provider: AttributeModifierFactory<BinaryAttributeValue> = AttributeModifierFactoryRegistry.getOrThrow(key)
        val modifiers: Map<out Attribute, AttributeModifier> = provider.createAttributeModifiers(uuid, value)
        return modifiers
    }

    override fun asShadowTag(): ShadowTag {
        val codec: AttributeCoreCodec<BinaryAttributeValue, SchemeAttributeValue> = AttributeCoreCodecRegistry.getOrThrow(key)
        val compound: CompoundShadowTag = codec.encode(value)
        return compound
    }
}