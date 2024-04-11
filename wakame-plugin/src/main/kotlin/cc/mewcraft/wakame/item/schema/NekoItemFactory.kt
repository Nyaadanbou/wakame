package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.item.EffectiveSlot
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

        //
        // Deserialize basic data
        //
        val uuid = root.node("uuid").krequire<UUID>()
        val material = root.node("material").krequire<Key>()
        val effectiveSlot = root.node("effective_slot").krequire<EffectiveSlot>()

        //
        // Deserialize item behaviors
        //
        val behaviors: List<String> = root.node("behaviors").childrenMap().mapNotNull { (key, _) -> key?.toString() }

        //
        // Deserialize item meta
        //
        val schemaMeta: ImmutableClassToInstanceMap<SchemaItemMeta<*>> = ImmutableClassToInstanceMap.builder<SchemaItemMeta<*>>().apply {
            // Side note 1: always put all schema metadata for a `NekoItem` even if the schema meta contains "nothing".
            // Side note 2: whether the data will be written to the item's NBT is decided by the realization process, not here.

            ItemMetaRegistry.Schema.reflections().forEach { reflect ->
                val clazz = reflect.clazz.java
                val path = reflect.path
                val itemMeta = root.node(path).require(clazz)
                put(clazz, itemMeta)
            }
        }.build()

        //
        // Deserialize item cells
        //
        val schemaCell: Map<String, SchemaCell> = buildMap {
            // loop through each cell node
            root.node("cells").childrenList().forEach { childNode ->
                // inject required hints
                childNode.hint(ITEM_ROOT_NODE_HINT, root)
                // deserialize the cell node
                val singleCell = childNode.krequire<SchemaCell>()
                // add it to the result map
                this += singleCell.id to singleCell
            }
        }

        return NekoItemImpl(
            key = key,
            uuid = uuid,
            config = provider,
            material = material,
            effectiveSlot = effectiveSlot,
            metaMap = schemaMeta,
            cellMap = schemaCell,
            behaviorHolders = behaviors
        )
    }
}