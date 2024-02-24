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
 *
 * @property skinOwner 物品的皮肤的所有者的 UUID
 */
data class SkinOwnerMeta(
    private val skinOwner: UUID? = null,
) : SchemeMeta<UUID> {
    override fun generate(context: SchemeGenerationContext): UUID? {
        return skinOwner
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "skin_owner")
    }
}

internal class SkinOwnerMetaSerializer : SchemeMetaSerializer<SkinOwnerMeta> {
    override val emptyValue: SkinOwnerMeta = SkinOwnerMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): SkinOwnerMeta {
        // TODO("Not yet implemented")
        return SkinOwnerMeta(null)
    }
}