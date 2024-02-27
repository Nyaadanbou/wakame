package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
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
data class SkinMeta(
    private val itemSkin: ItemSkin? = null,
) : SchemeItemMeta<ItemSkin> {
    override fun generate(context: SchemeGenerationContext): ItemSkin? {
        return itemSkin
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "skin")
    }
}

internal class SkinMetaSerializer : SchemeItemMetaSerializer<SkinMeta> {
    override val emptyValue: SkinMeta = SkinMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): SkinMeta {
        // TODO("Not yet implemented")
        return SkinMeta(null)
    }
}