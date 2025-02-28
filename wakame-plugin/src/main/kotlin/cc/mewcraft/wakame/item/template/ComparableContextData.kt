package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个可比较的上下文数据. 该接口仅为标记作用.
 *
 * 实现类必须重写 [equals] 和 [hashCode] 函数.
 */
interface ComparableContextData : Examinable


/* Implementations */


/**
 * 属性.
 */
class AttributeContextData(
    val id: String,
    val operation: AttributeModifier.Operation?,
    val element: RegistryEntry<ElementType>?,
) : ComparableContextData {
    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("operation", operation),
            ExaminableProperty.of("element", element),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is AttributeContextData)
            return false

        if (id != other.id)
            return false
        if (operation != other.operation)
            return false
        if (element != other.element)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (operation?.hashCode() ?: 0)
        result = 31 * result + (element?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 技能.
 */
class AbilityContextData(
    val id: Key,
) : ComparableContextData {
    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("id", id),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is AbilityContextData)
            return false

        if (id != other.id)
            return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
