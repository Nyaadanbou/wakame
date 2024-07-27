package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
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
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个元素类型.
 *
 * 使用 [ElementRegistry] 来获得该实例.
 */
interface Element : Keyed, Examinable, FriendlyNamed, BiIdentifiable<String, Byte>

/**
 * [Element] 的实现.
 */
private data class ElementType(
    override val uniqueId: String,
    override val binaryId: Byte,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : KoinComponent, Element {
    override val key: Key = Key.key("element", uniqueId)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", uniqueId),
        ExaminableProperty.of("binary", binaryId),
        ExaminableProperty.of("displayName", PlainTextComponentSerializer.plainText().serialize(displayName)),
        ExaminableProperty.of("styles", styles)
    )

    override fun hashCode(): Int = uniqueId.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Element) return other.uniqueId == uniqueId
        return false
    }

    override fun toString(): String = toSimpleString()
}

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: element:neutral
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * neutral:
 *   binary_index: 0
 *   display_name: 中立
 * ```
 */
internal object ElementSerializer : SchemaSerializer<Element> {
    override fun deserialize(type: Type, node: ConfigurationNode): Element {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            // TODO 更严格的解析方式
            val key = Key(scalar)
            return ElementRegistry.INSTANCES[key.value()]
        }

        // if it's structure 2
        val key = node.key().toString()
        val binary = node.node("binary_index").krequire<Int>().toStableByte()
        val displayName = node.node("display_name").krequire<Component>()
        val styles = node.node("styles").krequire<Array<StyleBuilderApplicable>>()
        return ElementType(key, binary, displayName, styles)
    }
}