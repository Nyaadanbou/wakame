package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的皮肤。
 *
 * @property itemSkin 物品的皮肤
 */
class SkinMeta(
    private val itemSkin: ItemSkin?,
) : SchemeMeta<ItemSkin> {
    override fun generate(context: SchemeGenerationContext): ItemSkin? {
        return itemSkin
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(SchemeMeta.ITEM_META_NAMESPACE, "skin")
    }
}

internal class SkinMetaSerializer : SchemeSerializer<SkinMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkinMeta {
        // TODO("Not yet implemented")
        if (node.virtual()) { // make it optional
            return SkinMeta(null)
        }

        return SkinMeta(null)
    }
}