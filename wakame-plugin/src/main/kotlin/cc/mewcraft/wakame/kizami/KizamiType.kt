package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.Collections.emptyMap
import java.util.stream.Stream

class KizamiType internal constructor(
    override val key: Key,
    override val stringId: String,
    // FIXME 移除, 持久化直接储存字符串 id 而非数字 id
    override val integerId: Int,
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
    val effectMap: Map<Int, List<KizamiEffect>>,
) : Kizami {
    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("key", key),
            ExaminableProperty.of("stringId", stringId),
            ExaminableProperty.of("integerId", integerId),
            ExaminableProperty.of("displayName", displayName),
            ExaminableProperty.of("styles", displayStyles)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * The serializer of kizami type.
 *
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   binary_index: <int>
 *   display_name: <string>
 *   styles: <string>
 *   effects:
 *     0:
 *       - type: <effect key>
 *         k1: v1
 *         k2: v2
 *         ...
 *       - type: <effect key>
 *         k1: v1
 *         k2: v2
 *     1:
 *       - type: <effect key>
 *         k1: v1
 *         k2: v2
 *         ...
 *       - type: <effect key>
 *         k1: v1
 *         k2: v2
 * ```
 */
internal object KizamiTypeSerializer : TypeSerializer<KizamiType> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiType {
        // get the kizami id we are dealing with
        val kizamiId = node.ownHint(RepresentationHints.KIZAMI_ID)
            ?: throw SerializationException(node, type, "No hint '${RepresentationHints.KIZAMI_ID}' is found in node '$node'")

        val stringId = kizamiId.value()
        val integerId = node.node("binary_index").require<Int>()
        val displayName = node.node("display_name").get<Component>(Component.text(stringId.replaceFirstChar(Char::uppercaseChar)))
        val displayStyles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        // the collection of effects on this kizami
        val effects = node.node("effects").get<Map<Int, List<KizamiEffect>>>(emptyMap())
        require(effects.keys.all { it >= 0 }) { "The keys of effects must be positive integers" }

        return KizamiType(
            kizamiId,
            stringId,
            integerId,
            displayName,
            displayStyles,
            effects
        )
    }
}
