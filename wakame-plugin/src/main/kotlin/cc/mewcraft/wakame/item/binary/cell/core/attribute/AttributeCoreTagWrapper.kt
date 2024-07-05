package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getByteOrNull
import net.kyori.adventure.key.Key

//
// 封装类（封装 NBT 对象），本身不储存数据
//

internal class BinaryAttributeCoreTagWrapperS(
    private val compound: CompoundTag,
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

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperR(
    private val compound: CompoundTag,
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

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperSE(
    private val compound: CompoundTag,
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

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

internal class BinaryAttributeCoreTagWrapperRE(
    private val compound: CompoundTag,
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

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

private fun CompoundTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundTag.getElement(): Element {
    return this.getByteOrNull(AttributeBinaryKeys.ELEMENT_TYPE)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundTag.getOperation(): AttributeModifier.Operation {
    return AttributeModifier.Operation.byId(this.getInt(AttributeBinaryKeys.OPERATION_TYPE))
}

private fun CompoundTag.getNumber(key: String): Double {
    return this.getDouble(key)
}