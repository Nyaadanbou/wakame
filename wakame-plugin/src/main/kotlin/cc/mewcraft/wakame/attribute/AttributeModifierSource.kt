package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key

/**
 * 代表一个可以提供属性修饰符的对象.
 *
 * ## 小米的思考
 * 设计这个接口是很难的. 这是因为构建一个 [AttributeModifier]
 * 需要一个 [Key] 作为修饰符的标识符, 但是这个 [Key] 的取值
 * 是一个比较复杂的问题.
 *
 * 因此, 我把构建一个 [AttributeModifier] 的责任分成了两部分:
 * 一部分是提供它的*数值*, 另一部分则是提供它的*标识*.
 *
 * 本接口的实现就是提供*数值*的那部分. 而*标识*那部分则是由
 * 调用者自身根据具体情况提供.
 */
interface AttributeModifierSource {
    /**
     * Provides one or more [AttributeModifiers][AttributeModifier] from this
     * object with the given [sourceId] being the modifiers' identifier. The returned
     * `map key` is an [Attribute] singleton and the `map value` is the attribute
     * modifier associated with the map key.
     *
     * @throws IllegalArgumentException if this provider can't provide such
     *   attribute modifiers
     */
    fun provideAttributeModifiers(sourceId: Key): Map<Attribute, AttributeModifier>
}
