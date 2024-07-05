package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.vanilla.VanillaComponentRemover
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.contains
import java.nio.file.Path
import java.util.UUID

object NekoItemFactory {
    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param relPath the relative path of the item in the configuration
     * @param root the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    fun create(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        val provider = NodeConfigProvider(root, relPath.toString())

        // read all basic info
        val uuid = root.node("uuid").krequire<UUID>()
        val itemType = root.node("item_type").krequire<Key>()
        val hideTooltip = root.node("hide_tooltip").getBoolean(false)
        val hideAdditionalTooltip = root.node("hide_additional_tooltip").getBoolean(false)
        val shownInTooltip = root.node("shown_in_tooltip").krequire<ShownInTooltipApplicator>()
        val removeComponents = root.node("remove_components").krequire<VanillaComponentRemover>()
        val slot = root.node("slot").krequire<ItemSlot>()

        // read all item behaviors
        val behaviorMap = ItemBehaviorMap.build {

            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                // 如果 root 里存在指定 id 的节点, 则添加对应的 behavior
                if (root.contains(path)) this.put(type, type.create())
            }

            tryAdd("castable", ItemBehaviorTypes.CASTABLE)
            tryAdd("chargeable", ItemBehaviorTypes.CHARGEABLE)
            tryAdd("damageable", ItemBehaviorTypes.DAMAGEABLE)
            tryAdd("enchantable", ItemBehaviorTypes.ENCHANTABLE)
            tryAdd("food", ItemBehaviorTypes.FOOD)
            tryAdd("tool", ItemBehaviorTypes.TOOL)
            tryAdd("trackable", ItemBehaviorTypes.TRACKABLE)
            tryAdd("wearable", ItemBehaviorTypes.WEARABLE)
        }

        // read all item templates (of item components)
        val templateMap = ItemTemplateMap.build {

            fun <T> tryAdd(path: String, type: ItemTemplateType<T>) {
                val node = root.node(path)
                val template = node.get(type.typeToken) ?: return
                this.put(type, template)
            }

            tryAdd("arrow", ItemTemplateTypes.ARROW)
            tryAdd("attributable", ItemTemplateTypes.ATTRIBUTABLE)
            tryAdd("castable", ItemTemplateTypes.CASTABLE)
            tryAdd("crate", ItemTemplateTypes.CRATE)
            tryAdd("custom_name", ItemTemplateTypes.CUSTOM_NAME)
            tryAdd("damageable", ItemTemplateTypes.DAMAGEABLE)
            tryAdd("lore", ItemTemplateTypes.LORE)
            tryAdd("fire_resistant", ItemTemplateTypes.FIRE_RESISTANT)
            tryAdd("food", ItemTemplateTypes.FOOD)
            tryAdd("cells", ItemTemplateTypes.CELLS)
            tryAdd("elements", ItemTemplateTypes.ELEMENTS)
            tryAdd("kizamiz", ItemTemplateTypes.KIZAMIZ)
            tryAdd("level", ItemTemplateTypes.LEVEL)
            tryAdd("item_name", ItemTemplateTypes.ITEM_NAME)
            tryAdd("rarity", ItemTemplateTypes.RARITY)
            // tryAdd("skin", ItemTemplateTypes.SKIN)
            // tryAdd("skin_owner", ItemTemplateTypes.SKIN_OWNER)
            tryAdd("kizamiable", ItemTemplateTypes.KIZAMIABLE)
            tryAdd("skillful", ItemTemplateTypes.SKILLFUL)
            tryAdd("tool", ItemTemplateTypes.TOOL)
            tryAdd("unbreakable", ItemTemplateTypes.UNBREAKABLE)
        }

        return NekoItemImpl(
            key = key,
            uuid = uuid,
            config = provider,
            itemType = itemType,
            hideTooltip = hideTooltip,
            hideAdditionalTooltip = hideAdditionalTooltip,
            shownInTooltip = shownInTooltip,
            slot = slot,
            removeComponents = removeComponents,
            templates = templateMap,
            behaviors = behaviorMap
        )
    }
}