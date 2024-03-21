package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import java.util.stream.Stream

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [ElementRegistry] to get the instances instead.
 */
data class Element @InternalApi internal constructor(
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
        if (other is Element) return other.uniqueId == uniqueId
        return false
    }
}