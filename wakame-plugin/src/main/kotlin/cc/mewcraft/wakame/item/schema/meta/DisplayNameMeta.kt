package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
sealed interface SDisplayNameMeta : SchemaItemMeta<String> {
    override val key: Key get() = ItemMetaKeys.DISPLAY_NAME
}

private class NonNullDisplayNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val displayName: String,
) : SDisplayNameMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> {
        return GenerationResult(displayName)
    }
}

private data object DefaultDisplayNameMeta : SDisplayNameMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> = GenerationResult.empty()
}

internal data object DisplayNameMetaSerializer : SchemaItemMetaSerializer<SDisplayNameMeta> {
    override val defaultValue: SDisplayNameMeta = DefaultDisplayNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayNameMeta {
        return NonNullDisplayNameMeta(node.krequire<String>())
    }
}