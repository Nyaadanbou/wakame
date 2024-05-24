package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
@ConfigPath(ItemMetaConstants.CUSTOM_NAME)
sealed interface SCustomNameMeta : SchemaItemMeta<String> {
    override val key: Key get() = ItemMetaConstants.createKey { CUSTOM_NAME }
}

private class NonNullCustomNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val customName: String,
) : SCustomNameMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> {
        return GenerationResult(customName)
    }
}

private data object DefaultCustomNameMeta : SCustomNameMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> = GenerationResult.empty()
}

internal data object DisplayNameMetaSerializer : SchemaItemMetaSerializer<SCustomNameMeta> {
    override val defaultValue: SCustomNameMeta = DefaultCustomNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SCustomNameMeta {
        return NonNullCustomNameMeta(node.krequire<String>())
    }
}