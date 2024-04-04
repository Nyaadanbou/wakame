package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
sealed interface SSkinOwnerMeta : SchemaItemMeta<UUID> {
    override val key: Key get() = ItemMetaKeys.SKIN_OWNER
}

private class NonNullSkinOwnerMeta(
    /**
     * 物品的皮肤的所有者的 UUID.
     */
    private val skinOwner: UUID,
) : SSkinOwnerMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<UUID> {
        return GenerationResult(skinOwner)
    }
}

private data object DefaultSkinOwnerMeta : SSkinOwnerMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<UUID> = GenerationResult.empty()
}

internal data object SkinOwnerMetaSerializer : SchemaItemMetaSerializer<SSkinOwnerMeta> {
    override val defaultValue: SSkinOwnerMeta = DefaultSkinOwnerMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SSkinOwnerMeta {
        return DefaultSkinOwnerMeta // TODO returns a non-empty value
    }
}