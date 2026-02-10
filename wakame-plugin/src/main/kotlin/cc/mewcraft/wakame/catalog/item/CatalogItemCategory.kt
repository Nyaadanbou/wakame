package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import xyz.xenondevs.invui.gui.structure.Marker
import xyz.xenondevs.invui.gui.structure.Markers
import java.lang.reflect.Type

/**
 * 物品图鉴中展示的一个物品类别.
 */
data class CatalogItemCategory(
    val id: Identifier,
    val icon: Identifier,
    val menuSettings: BasicMenuSettings,
    val contentMarker: Marker,
    val permission: String?,
    val items: List<ItemRef>,
) {
    enum class ContentMarker {
        HORIZONTAL,
        VERTICAL,
    }
}

/**
 * [CatalogItemCategory] 的序列化器.
 */
internal object CategorySerializer : SimpleSerializer<CatalogItemCategory> {

    override fun deserialize(type: Type, node: ConfigurationNode): CatalogItemCategory {
        val id = node.hint(RepresentationHints.CATAGORY_ID) ?: throw SerializationException(
            "The hint ${RepresentationHints.CATAGORY_ID.identifier()} is not present"
        )

        val icon = node.node("icon").require<Key>()
        val permission = node.node("permission").get<String>()
        val settings = node.node("menu_settings").require<BasicMenuSettings>()
        val contentMarker = when (node.node("content_marker").get<CatalogItemCategory.ContentMarker>(CatalogItemCategory.ContentMarker.HORIZONTAL)) {
            CatalogItemCategory.ContentMarker.HORIZONTAL -> Markers.CONTENT_LIST_SLOT_HORIZONTAL
            CatalogItemCategory.ContentMarker.VERTICAL -> Markers.CONTENT_LIST_SLOT_VERTICAL
        }

        // val itemIds = node.node("items").getList<ItemX>(emptyList())
        // 不像上面这样写的原因: 若列表中的某个 id 有问题, 将跳过这个 id 而不是抛异常
        val itemIds = node.node("items").getList<String>(emptyList())
        val itemList = mutableListOf<ItemRef>()
        for (itemId in itemIds) {
            val item = ItemRef.create(Identifiers.of(itemId))
            if (item == null) {
                LOGGER.warn("Cannot deserialize string '$itemId' into ItemRef, skipped adding it to category: '$itemId'")
                continue
            }
            itemList.add(item)
        }
        return CatalogItemCategory(id, icon, settings, contentMarker, permission, itemList)
    }
}

