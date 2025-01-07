package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableByte
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * [Kizami] 的实现.
 */
private data class KizamiType(
    override val uniqueId: String,
    override val binaryId: Byte,
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
) : KoinComponent, Kizami {
    override val key: Key = Key.key(Namespaces.KIZAMI, uniqueId)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("uniqueId", uniqueId),
        ExaminableProperty.of("binaryId", binaryId),
        ExaminableProperty.of("displayName", PlainTextComponentSerializer.plainText().serialize(displayName)),
        ExaminableProperty.of("styles", displayStyles)
    )

    override fun hashCode(): Int = uniqueId.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Kizami) return other.uniqueId == uniqueId
        return false
    }

    override fun toString(): String = toSimpleString()
}

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: kizami:iron
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * iron:
 *   binary_index: 0
 *   display_name: 中立
 *   ...
 * ```
 */
internal object KizamiSerializer : SchemaSerializer<Kizami> {
    /**
     * 用来传递铭刻的 id.
     */
    // 开发日记 2024/8/31 小米
    // 因为铭刻现在是“一文件, 一铭刻” 所以文件内部无从得知铭刻的 id.
    // 我们需要通过 hint 将铭刻 id (也就是文件名) 传递给序列化器.
    val HINT_ID: RepresentationHint<String> = RepresentationHint.of("id", String::class.java)

    /**
     * The key hint is used to pass the kizami key to the child
     * node deserialization, such as the deserialization of `effects`.
     */
    val HINT_KEY: RepresentationHint<Key> = RepresentationHint.of("key", Key::class.java)

    override fun deserialize(type: Type, node: ConfigurationNode): Kizami {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            // TODO 更严格的解析方式
            val key = Key(scalar)
            return KizamiRegistry.INSTANCES[key.value()]
        }

        // if it's structure 2
        val uniqueId = node.hint(HINT_ID) ?: throw IllegalStateException("No provided hint for kizami id. This is a bug!")
        val binaryId = node.node("binary_index").krequire<Int>().toStableByte()
        val displayName = node.node("display_name").krequire<Component>()
        val styles = node.node("styles").krequire<Array<StyleBuilderApplicable>>()
        return KizamiType(uniqueId, binaryId, displayName, styles)
    }
}