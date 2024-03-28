package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
sealed interface SkinOwnerMeta : SchemeItemMeta<UUID> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "skin_owner")
    }
}

private class NonNullSkinOwnerMeta(
    /**
     * 物品的皮肤的所有者的 UUID.
     */
    private val skinOwner: UUID,
) : SkinOwnerMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<UUID> {
        return skinOwner.toMetaResult()
    }
}

private data object DefaultSkinOwnerMeta : SkinOwnerMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<UUID> = nonGenerate()
}

internal class SkinOwnerMetaSerializer : SchemeItemMetaSerializer<SkinOwnerMeta> {
    override val defaultValue: SkinOwnerMeta = DefaultSkinOwnerMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SkinOwnerMeta {
        return DefaultSkinOwnerMeta // TODO returns a non-empty value
    }
}