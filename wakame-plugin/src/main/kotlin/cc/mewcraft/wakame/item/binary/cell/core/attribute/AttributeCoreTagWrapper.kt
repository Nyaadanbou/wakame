package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getByteOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.UUID

//
// 封装类（封装 NBT 对象），本身不储存数据
//

internal class BinaryAttributeCoreTagWrapperS(
    val compound: CompoundShadowTag,
) : BinaryAttributeCoreS() {
    override val key: Key
        get() = compound.getIdentifier()
    override val operation: AttributeModifier.Operation
        get() = compound.getOperation()
    override val value: Double
        get() = compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE)

    override fun clear() {
        compound.tags().clear()
    }

    override fun asTag(): ShadowTag {
        return compound
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperR(
    val compound: CompoundShadowTag,
) : BinaryAttributeCoreR() {
    override val key: Key
        get() = compound.getIdentifier()
    override val operation: AttributeModifier.Operation
        get() = compound.getOperation()
    override val lower: Double
        get() = compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE)
    override val upper: Double
        get() = compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE)

    override fun clear() {
        compound.tags().clear()
    }

    override fun asTag(): ShadowTag {
        return compound
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperSE(
    val compound: CompoundShadowTag,
) : BinaryAttributeCoreSE() {
    override val key: Key
        get() = compound.getIdentifier()
    override val operation: AttributeModifier.Operation
        get() = compound.getOperation()
    override val value: Double
        get() = compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE)
    override val element: Element
        get() = compound.getElement()

    override fun clear() {
        compound.tags().clear()
    }

    override fun asTag(): ShadowTag {
        return compound
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperRE(
    val compound: CompoundShadowTag,
) : BinaryAttributeCoreRE() {
    override val key: Key
        get() = compound.getIdentifier()
    override val operation: AttributeModifier.Operation
        get() = compound.getOperation()
    override val lower: Double
        get() = compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE)
    override val upper: Double
        get() = compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE)
    override val element: Element
        get() = compound.getElement()

    override fun clear() {
        compound.tags().clear()
    }

    override fun asTag(): ShadowTag {
        return compound
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return compound.asString()
    }
}

private fun CompoundShadowTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundShadowTag.getElement(): Element {
    return this.getByteOrNull(AttributeBinaryKeys.ELEMENT_TYPE)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundShadowTag.getOperation(): AttributeModifier.Operation {
    return AttributeModifier.Operation.byId(this.getInt(AttributeBinaryKeys.OPERATION_TYPE))
}

private fun CompoundShadowTag.getNumber(key: String): Double {
    return this.getDouble(key)
}