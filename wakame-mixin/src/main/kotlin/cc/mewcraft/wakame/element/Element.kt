package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
import cc.mewcraft.wakame.util.adventure.plain
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.jetbrains.annotations.ApiStatus
import java.util.stream.Stream

/**
 * 代表一个元素类型.
 */
class Element
@ApiStatus.Internal
constructor(
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
) : Keyed, Examinable, PlayerFriendlyNamed {

    override fun key(): Key {
        return BuiltInRegistries.ELEMENT.getId(this) ?: KoishKeys.of("unregistered")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key()),
        ExaminableProperty.of("displayName", displayName.plain),
        ExaminableProperty.of("displayStyles", displayStyles),
    )

    override fun toString(): String = toSimpleString()
}