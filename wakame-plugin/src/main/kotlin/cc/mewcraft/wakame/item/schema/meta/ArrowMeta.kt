package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.stream.Stream

data class Arrow(
    val pierceLevel: Byte? = null,
) : Examinable {
    init {
        pierceLevel?.run { require(this >= 0) { "pierceLevel >= 0" } }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("pierceLevel", pierceLevel),
    )

    override fun toString(): String = toSimpleString()
}


@ConfigPath(ItemMetaConstants.ARROW)
sealed interface SArrowMeta : SchemaItemMeta<Arrow> {
    override val key: Key get() = ItemMetaConstants.createKey { ARROW }
}

private class NonNullArrowMeta(
    private val pierceLevel: Byte? = null,
) : SArrowMeta {
    init {
        pierceLevel?.run { require(this >= 0) { "pierceLevel >= 0" } }
    }

    override val isEmpty: Boolean = false

    override fun generate(context: SchemaGenerationContext): GenerationResult<Arrow> {
        return GenerationResult(Arrow(pierceLevel))
    }
}

private data object DefaultArrowMeta : SArrowMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<Arrow> = GenerationResult.empty()
}

internal data object ArrowMetaSerializer : SchemaItemMetaSerializer<SArrowMeta> {
    override val defaultValue: SArrowMeta = DefaultArrowMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SArrowMeta {
        val pierceLevel = node.node("pierce_level").get<Byte>()
        return NonNullArrowMeta(pierceLevel)
    }
}
