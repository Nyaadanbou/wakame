package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.jetbrains.annotations.ApiStatus
import java.util.stream.Stream

/**
 * 代表一种攻击速度.
 *
 * @property cooldown 攻击冷却时长, 单位: tick
 */
class AttackSpeed
@ApiStatus.Internal
constructor(
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    val cooldown: Int,
) : Keyed, Examinable, PlayerFriendlyNamed {

    override fun key(): Identifier {
        return BuiltInRegistries.ATTACK_SPEED.getId(this) ?: Identifiers.of("unregistered")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("key", key()),
            ExaminableProperty.of("displayName", displayName),
            ExaminableProperty.of("displayStyles", displayStyles),
            ExaminableProperty.of("cooldown", cooldown),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }

    companion object {

        @JvmField
        val INTRINSIC: AttackSpeed = AttackSpeed(Component.text("intrinsic"), emptyArray(), 10)

    }
}
