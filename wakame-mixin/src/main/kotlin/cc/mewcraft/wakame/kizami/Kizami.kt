package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
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
 * 代表一个铭刻类型.
 */
class Kizami
@ApiStatus.Internal
constructor(
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,

    /**
     * 铭刻的效果.
     *
     * 键名 [Int] 代表产生对应铭刻效果 [KizamiEffect] 所需要的铭刻数量.
     *
     * 例如有这样一个映射:
     * - `1` -> [`增加 2 攻击力`]
     * - `2` -> [`增加 3 攻击力`]
     * - `3` -> [`增加 4 攻击力`, `激活被动技能`]
     *
     * 意为:
     * - 当铭刻数量达到 1 时, 会产生 `增加 2 攻击力` 的效果;
     * - 当铭刻数量达到 2 时, 会产生 `增加 3 攻击力` 的效果;
     * - 当铭刻数量达到 3 时, 会产生 `增加 4 攻击力` 和 `激活被动技能` 的效果.
     */
    val effects: Map<Int, List<KizamiEffect>>,
) : Keyed, Examinable, PlayerFriendlyNamed {

    override fun key(): Key {
        return BuiltInRegistries.KIZAMI.getId(this) ?: KoishKeys.of("unregistered")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("key", key()),
            ExaminableProperty.of("displayName", displayName),
            ExaminableProperty.of("displayStyles", displayStyles)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
