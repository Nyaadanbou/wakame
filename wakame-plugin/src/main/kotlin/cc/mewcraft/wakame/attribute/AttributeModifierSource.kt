package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import net.kyori.adventure.key.Key

/**
 * 代表一个可以提供属性修饰符的对象.
 *
 * ## 小米的思考
 * 设计这个接口是很难的. 这是因为构建一个 [cc.mewcraft.wakame.entity.attribute.AttributeModifier]
 * 需要一个 [Key] 作为修饰符的标识符, 但是这个 [Key] 的取值
 * 是一个比较复杂的问题.
 *
 * 因此, 我把构建一个 [cc.mewcraft.wakame.entity.attribute.AttributeModifier] 的责任分成了两部分:
 * 一部分是提供它的*数值*, 另一部分则是提供它的*标识*.
 *
 * 本接口的实现就是提供*数值*的那部分. 而*标识*那部分则是由
 * 调用者自身根据具体情况提供.
 */
interface AttributeModifierSource {
    /**
     * Creates a map of [cc.mewcraft.wakame.entity.attribute.AttributeModifier] from this object with the given [modifierId]
     * being the modifiers' identifier. The returned `map key` is an [cc.mewcraft.wakame.entity.attribute.Attribute] instance
     * and the `map value` is the [cc.mewcraft.wakame.entity.attribute.AttributeModifier] associated with the map key.
     */
    fun createAttributeModifiers(modifierId: Key): Map<Attribute, AttributeModifier>
}
