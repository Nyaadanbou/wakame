package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.core.NumberRepresentable
import cc.mewcraft.wakame.core.PlayerFriendlyNamed
import cc.mewcraft.wakame.core.StringRepresentable
import cc.mewcraft.wakame.util.plain
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个稀有度类型.
 */
interface Rarity : Keyed, Examinable, PlayerFriendlyNamed, StringRepresentable, NumberRepresentable, Comparable<Rarity> {
    val weight: Int
    val glowColor: GlowColor
}

/**
 * [Rarity] 的实现.
 */
class RarityType
internal constructor(
    override val key: Key,
    override val stringId: String,
    override val integerId: Int,
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    override val weight: Int,
    override val glowColor: GlowColor,
) : Rarity {
    override fun compareTo(other: Rarity): Int {
        return weight.compareTo(other.weight)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("stringId", stringId),
        ExaminableProperty.of("integerId", integerId),
        ExaminableProperty.of("displayName", displayName.plain),
        ExaminableProperty.of("displayStyles", displayStyles),
        ExaminableProperty.of("weight", weight),
        ExaminableProperty.of("glowColor", glowColor),
    )

    override fun toString(): String = toSimpleString()
}
