package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
import cc.mewcraft.wakame.util.adventure.plain
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.jetbrains.annotations.ApiStatus
import java.util.stream.Stream

/**
 * 物品的稀有度.
 *
 * @property weight 稀有度的权重
 * @property color 稀有度的颜色, 目前仅用于高亮掉落物
 * @property enchantSlotBase 附魔栏位的基础值
 * @property enchantSlotLimit 附魔栏位的上限值
 */
class Rarity
@ApiStatus.Internal
constructor(
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    val weight: Int,
    val color: NamedTextColor?,
    val enchantSlotBase: Int,
    val enchantSlotLimit: Int,
) : Keyed, Examinable, PlayerFriendlyNamed, Comparable<Rarity> {

    override fun key(): Key {
        return BuiltInRegistries.RARITY.getId(this) ?: KoishKeys.of("unregistered")
    }

    override fun compareTo(other: Rarity): Int {
        return weight.compareTo(other.weight)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key()),
        ExaminableProperty.of("displayName", displayName.plain),
        ExaminableProperty.of("displayStyles", displayStyles),
        ExaminableProperty.of("weight", weight),
        ExaminableProperty.of("color", color),
        ExaminableProperty.of("enchantSlotBase", enchantSlotBase),
        ExaminableProperty.of("enchantSlotLimit", enchantSlotLimit),
    )

    override fun toString(): String = toSimpleString()
}
