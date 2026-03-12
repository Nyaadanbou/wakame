package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import xyz.xenondevs.invui.gui.structure.Marker
import xyz.xenondevs.invui.gui.structure.Markers

/**
 * 物品图鉴中展示的一个物品类别.
 */
data class CatalogItemCategory(
    val id: KoishKey,
    val icon: KoishKey,
    val menuSettings: BasicMenuSettings,
    val contentMarker: Marker,
    val permission: String?,
    val items: List<ItemRef>,
) {
    enum class ContentMarker {
        HORIZONTAL,
        VERTICAL,
    }

    companion object {
        fun serializer(): SimpleSerializer<CatalogItemCategory> = SimpleSerializer { _, node ->
            val id = node.hint(RepresentationHints.CATAGORY_ID) ?: throw SerializationException(
                "The hint ${RepresentationHints.CATAGORY_ID.identifier()} is not present"
            )

            val icon = node.node("icon").require<Key>()
            val permission = node.node("permission").get<String>()
            val settings = CatalogItemMenuSettings.getMenuSettings("category/${id.value()}")
            val contentMarker = when (node.node("content_marker").get<ContentMarker>(ContentMarker.HORIZONTAL)) {
                ContentMarker.HORIZONTAL -> Markers.CONTENT_LIST_SLOT_HORIZONTAL
                ContentMarker.VERTICAL -> Markers.CONTENT_LIST_SLOT_VERTICAL
            }

            // val itemIds = node.node("items").getList<ItemRef>(emptyList())
            // 不像上面这样写的原因: 若列表中的某个 id 有问题, 将跳过这个 id 而不是抛异常
            val itemIds = node.node("items").getList<String>(emptyList())
            val itemRefs = mutableListOf<ItemRef>()
            for (itemId in itemIds) {
                val itemRef = ItemRef.create(KoishKeys.of(itemId))
                if (itemRef == null) {
                    LOGGER.warn("Cannot deserialize string '$itemId' into ItemRef, skipped adding it to category: '$itemId'")
                    continue
                }
                itemRefs.add(itemRef)
            }
            CatalogItemCategory(id, icon, settings, contentMarker, permission, itemRefs)
        }
    }
}