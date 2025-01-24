package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXRegistry
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 指导书中展示的一个物品类别.
 */
data class Category(
    val id: String,
    val icon: Key,
    val menuSettings: BasicMenuSettings,
    val items: List<ItemX>,
) {
}

/**
 * [Category] 的序列化器.
 */
internal object CategorySerializer : TypeSerializer<Category>, KoinComponent {
    private val logger: Logger by inject()
    val HINT_NODE: RepresentationHint<String> = RepresentationHint.of("id", typeTokenOf<String>())
    override fun deserialize(type: Type, node: ConfigurationNode): Category {
        val id = node.hint(HINT_NODE) ?: throw SerializationException(
            "The hint node for category id is not present"
        )

        val icon = node.node("icon").krequire<Key>()
        val settings = node.node("menu_settings").krequire<BasicMenuSettings>()

        val itemUids = node.node("items").getList<String>(emptyList())
        val list: MutableList<ItemX> = mutableListOf()
        for (uid in itemUids) {
            val itemX = ItemXRegistry[uid]
            if (itemX == null) {
                logger.warn("Cannot deserialize string '$uid' into ItemX. Skip add it to category: '$id'")
                continue
            }
            list.add(itemX)
        }
        return Category(id, icon, settings, list)
    }
}

