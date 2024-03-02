package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
interface SkinOwnerMeta : SchemeItemMeta<UUID> {
    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "skin_owner")
    }
}

private class NonNullSkinOwnerMeta(
    /**
     * 物品的皮肤的所有者的 UUID.
     */
    private val skinOwner: UUID,
) : SkinOwnerMeta {
    override fun generate(context: SchemeGenerationContext): UUID? {
        return skinOwner
    }
}

private object DefaultSkinOwnerMeta : SkinOwnerMeta {
    override fun generate(context: SchemeGenerationContext): UUID? {
        return null
    }
}

internal class SkinOwnerMetaSerializer : SchemeItemMetaSerializer<SkinOwnerMeta> {
    override val defaultValue: SkinOwnerMeta = DefaultSkinOwnerMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SkinOwnerMeta {
        return DefaultSkinOwnerMeta // TODO returns a non-empty value
    }
}