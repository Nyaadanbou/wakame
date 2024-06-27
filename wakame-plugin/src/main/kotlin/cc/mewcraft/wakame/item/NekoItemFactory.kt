package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.registry.ItemMetaRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import com.google.common.collect.ImmutableClassToInstanceMap
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import java.nio.file.Path
import java.util.UUID

object NekoItemFactory {
    val ITEM_ROOT_NODE_HINT: RepresentationHint<ConfigurationNode> = RepresentationHint.of("item_root_node", typeTokenOf<ConfigurationNode>())

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

        // required
        val uuid = root.node("uuid").krequire<UUID>()
        // required
        val itemType = root.node("item_type").krequire<Key>()
        // optional
        val hideTooltip = root.node("hide_tooltip").getBoolean(false)
        // optional
        val hideAdditionalTooltip = root.node("hide_additional_tooltip").getBoolean(false)
        // optional
        val shownInTooltip = root.node("shown_in_tooltip").krequire<ShownInTooltipApplicator>()
        // optional
        val slot = root.node("slot").krequire<ItemSlot>()
        // optional
        val behaviors = root.node("behaviors").childrenMap().mapNotNull { (key, _) -> key?.toString() }

        val schemaMeta = ImmutableClassToInstanceMap.builder<SchemaItemMeta<*>>().apply {
            // Side note 1: always put all schema metadata for a `NekoItem` even if the schema meta contains "nothing".
            // Side note 2: whether the data will be written to the item's NBT is decided by the realization process, not here.

            ItemMetaRegistry.Schema.reflections()
                .associate { reflect ->
                    reflect.clazz to reflect.path
                }
                .forEach { (clazz, path) ->
                    val javaClass = clazz.java
                    val itemMeta = root.node(path).krequire(clazz)
                    put(javaClass, itemMeta)
                }
        }.build()

        val schemaCell = buildMap {
            // loop through each cell node
            root.node("cells").childrenMap()
                .mapKeys { it.key.toString() }
                .forEach { (id, childNode) ->
                    // inject required hints
                    childNode.hint(ITEM_ROOT_NODE_HINT, root)
                    // deserialize the cell node
                    val cell = childNode.krequire<SchemaCell>()
                    // add it to the result map
                    this += (id to cell)
                }
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
        )
    }
}