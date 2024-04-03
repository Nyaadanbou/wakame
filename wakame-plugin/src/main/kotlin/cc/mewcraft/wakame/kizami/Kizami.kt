package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableByte
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import java.lang.reflect.Type
import java.util.UUID
import java.util.stream.Stream

/**
 * This class solely serves the purpose of identifying kizami.
 *
 * **DO NOT CONSTRUCT IT YOURSELF!** Use [KizamiRegistry] to get the instances instead.
 */
data class Kizami @InternalApi internal constructor(
    val uuid: UUID,
    override val uniqueId: String,
    override val binaryId: Byte,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : KoinComponent, FriendlyNamed, BiIdentifiable<String, Byte>, Examinable {
    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", uniqueId),
            ExaminableProperty.of("binary", binaryId),
            ExaminableProperty.of("displayName", PlainTextComponentSerializer.plainText().serialize(displayName)),
            ExaminableProperty.of("styles", styles)
        )
    }

    override fun toString(): String = toSimpleString()
    override fun hashCode(): Int = uniqueId.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Kizami) return other.uniqueId == uniqueId
        return false
    }
}

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: iron
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
     * The UUID hint is used to pass the kizami UUID to the child
     * node deserialization, such as the deserialization of `effects`.
     */
    val UUID_HINT: RepresentationHint<UUID> = RepresentationHint.of("uuid", UUID::class.java)

    override fun deserialize(type: Type, node: ConfigurationNode): Kizami {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            return KizamiRegistry.INSTANCES[scalar]
        }

        // if it's structure 2
        val uuid = node.node("uuid").krequire<UUID>()
        val key = node.key().toString()
        val binary = node.node("binary_index").krequire<Int>().toStableByte()
        val displayName = node.node("display_name").krequire<Component>()
        val styles = node.node("styles").krequire<Array<StyleBuilderApplicable>>()
        return (@OptIn(InternalApi::class) Kizami(uuid, key, binary, displayName, styles))
    }
}