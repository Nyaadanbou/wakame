package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.PlayerFriendlyNamed
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableByte
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个稀有度类型.
 *
 * 使用 [RarityRegistry] 来获得该实例.
 */
interface Rarity : Keyed, Examinable, PlayerFriendlyNamed, BiIdentifiable<String, Byte>, Comparable<Rarity> {
    val weight: Int
    val glowColor: GlowColor
}

/**
 * [Rarity] 的实现.
 */
private data class RarityType(
    override val uniqueId: String,
    override val binaryId: Byte,
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    override val weight: Int,
    override val glowColor: GlowColor,
) : KoinComponent, Rarity {
    override val key: Key = Key.key(Namespaces.RARITY, uniqueId)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("uniqueId", uniqueId),
        ExaminableProperty.of("binaryId", binaryId),
        ExaminableProperty.of("displayName", PlainTextComponentSerializer.plainText().serialize(displayName)),
        ExaminableProperty.of("styles", displayStyles),
        ExaminableProperty.of("weight", weight),
        ExaminableProperty.of("glowColor", glowColor)
    )

    override fun compareTo(other: Rarity): Int {
        return weight.compareTo(other.weight)
    }

    override fun hashCode(): Int = uniqueId.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Rarity) return other.uniqueId == uniqueId
        return false
    }

    override fun toString(): String = toSimpleString()
}

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: epic
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * epic:
 *   binary_index: 3
 *   display_name: 史诗
 *   ...
 * ```
 */
internal object RaritySerializer : TypeSerializer<Rarity> {
    override fun deserialize(type: Type, node: ConfigurationNode): Rarity {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            return RarityRegistry.INSTANCES[scalar]
        }

        // if it's structure 2
        val key = node.key().toString()
        val binary = node.node("binary_index").krequire<Int>().toStableByte()
        val displayName = node.node("display_name").krequire<Component>()
        val styles = node.node("styles").krequire<Array<StyleBuilderApplicable>>()
        val weight = node.node("weight").get<Int>(0)
        val glowColor = node.node("glow_color").krequire<GlowColor>()
        return RarityType(key, binary, displayName, styles, weight, glowColor)
    }
}