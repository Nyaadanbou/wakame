package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
sealed interface SSkinOwnerMeta : SchemaItemMeta<UUID> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "skin_owner")
    }
}

private class NonNullSkinOwnerMeta(
    /**
     * 物品的皮肤的所有者的 UUID.
     */
    private val skinOwner: UUID,
) : SSkinOwnerMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<UUID> {
        return GenerationResult(skinOwner)
    }
}

private data object DefaultSkinOwnerMeta : SSkinOwnerMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<UUID> = GenerationResult.empty()
}

internal class SkinOwnerMetaSerializer : SchemaItemMetaSerializer<SSkinOwnerMeta> {
    override val defaultValue: SSkinOwnerMeta = DefaultSkinOwnerMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SSkinOwnerMeta {
        return DefaultSkinOwnerMeta // TODO returns a non-empty value
    }
}