package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.util.adventure.plain
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * [Element] 的实现.
 */
class ElementType(
    override val key: Key,
    override val stringId: String,
    override val integerId: Int,
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    val stackEffect: StackEffect?,
) : Element {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("stringId", stringId),
        ExaminableProperty.of("integerId", integerId),
        ExaminableProperty.of("displayName", displayName.plain),
        ExaminableProperty.of("styles", displayStyles),
        ExaminableProperty.of("stackEffect", stackEffect),
    )

    override fun toString(): String = toSimpleString()
}