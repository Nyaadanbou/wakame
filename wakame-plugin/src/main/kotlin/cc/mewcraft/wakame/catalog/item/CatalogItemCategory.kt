package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品图鉴中展示的一个物品类别.
 */
data class CatalogItemCategory(
    val id: Identifier,
    val icon: Key,
    val menuSettings: BasicMenuSettings,
    val permission: String?,
    val items: List<ItemRef>,
)

/**
 * [CatalogItemCategory] 的序列化器.
 */
internal object CategorySerializer : TypeSerializer2<CatalogItemCategory> {

    override fun deserialize(type: Type, node: ConfigurationNode): CatalogItemCategory {
        val id = node.hint(RepresentationHints.CATAGORY_ID) ?: throw SerializationException(
            "The hint ${RepresentationHints.CATAGORY_ID.identifier()} is not present"
        )

        val icon = node.node("icon").require<Key>()
        val permission = node.node("permission").get<String>()
        val settings = node.node("menu_settings").require<BasicMenuSettings>()

        // val itemUids = node.node("items").getList<ItemX>(emptyList())
        // 不像上面这样写的原因: 若列表中的某个 id 有问题, 将跳过这个 id 而不是抛异常
        val itemIds = node.node("items").getList<String>(emptyList())
        val itemList = mutableListOf<ItemRef>()
        for (uid in itemIds) {
            val item = ItemRef.create(Identifiers.of(uid))
            if (item == null) {
                LOGGER.warn("Cannot deserialize string '$uid' into ItemX, skipped adding it to category: '$id'")
                continue
            }
            itemList.add(item)
        }
        return CatalogItemCategory(id, icon, settings, permission, itemList)
    }

}

