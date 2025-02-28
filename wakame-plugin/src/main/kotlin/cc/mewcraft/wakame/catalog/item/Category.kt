package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXFactoryRegistry
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品图鉴中展示的一个物品类别.
 */
data class Category(
    val id: Identifier,
    val icon: Key,
    val menuSettings: BasicMenuSettings,
    val permission: String?,
    val items: List<ItemX>,
)

/**
 * [Category] 的序列化器.
 */
internal object CategorySerializer : TypeSerializer<Category>, KoinComponent {

    override fun deserialize(type: Type, node: ConfigurationNode): Category {
        val id = node.hint(RepresentationHints.CATAGORY_ID) ?: throw SerializationException(
            "The hint ${RepresentationHints.CATAGORY_ID.identifier()} is not present"
        )

        val icon = node.node("icon").require<Key>()
        val settings = node.node("menu_settings").require<BasicMenuSettings>()
        val permission = node.get<String>("permission")

        // val itemUids = node.node("items").getList<ItemX>(emptyList())
        // 不像上面这样写的原因: 若列表中的某个 id 有问题, 将跳过这个 id 而不是抛异常
        val itemIds = node.node("items").getList<String>(emptyList())
        val itemList = mutableListOf<ItemX>()
        for (uid in itemIds) {
            val item = ItemXFactoryRegistry[uid]
            if (item == null) {
                LOGGER.warn("Cannot deserialize string '$uid' into ItemX, skipped adding it to category: '$id'")
                continue
            }
            itemList.add(item)
        }
        return Category(id, icon, settings, permission, itemList)
    }

}

