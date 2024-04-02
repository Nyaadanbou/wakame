package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.cell.SchemaCellFactory
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.provider.NodeConfigProvider
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.ImmutableClassToInstanceMap
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.nio.file.Path
import java.util.UUID

object NekoItemFactory {
    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param root the configuration node holding the data of the item
     * @param relPath the relative path of the item in the configuration
     * @return a new [NekoItem]
     */
    fun create(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        val provider = NodeConfigProvider(root, relPath.toString())

        // Deserialize basic data
        val uuid = root.node("uuid").requireKt<UUID>()
        val material = root.node("material").requireKt<Key>()
        val effectiveSlot = root.node("effective_slot").requireKt<EffectiveSlot>()

        // Deserialize item behaviors
        val behaviors: List<String> = root.node("behaviors").childrenMap().mapNotNull { (key, _) -> key?.toString() }

        // Deserialize standalone item meta
        val schemaMeta: ImmutableClassToInstanceMap<SchemaItemMeta<*>> = ImmutableClassToInstanceMap.builder<SchemaItemMeta<*>>().apply {
            // Side note 1: buildMap preserves the insertion order
            // Side note 2: always put all schema metadata for a `NekoItem` even if the schema meta contains "nothing".
            // Side note 3: whether the data will be written to the item's NBT is decided by the realization process, not here.

            // Write it in alphabet order, in case you miss something
            loadAndSave<SDisplayLoreMeta>(root, ItemMetaKeys.DISPLAY_LORE)
            loadAndSave<SDisplayNameMeta>(root, ItemMetaKeys.DISPLAY_NAME)
            loadAndSave<SDurabilityMeta>(root, ItemMetaKeys.DURABILITY)
            loadAndSave<SElementMeta>(root, ItemMetaKeys.ELEMENT)
            loadAndSave<SKizamiMeta>(root, ItemMetaKeys.KIZAMI)
            loadAndSave<SLevelMeta>(root, ItemMetaKeys.LEVEL)
            loadAndSave<SRarityMeta>(root, ItemMetaKeys.RARITY)
            loadAndSave<SSkinMeta>(root, ItemMetaKeys.SKIN)
            loadAndSave<SSkinOwnerMeta>(root, ItemMetaKeys.SKIN_OWNER)
        }.build()

        // Deserialize item cells
        val schemaCell: Map<String, SchemaCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            // A convenient local func
            fun constructNode(path: String, sourceNode: ConfigurationNode): ConfigurationNode? {
                return sourceNode.node(path).string
                    // get the "... groups" node
                    ?.let { root.node("${path}_groups", it) }
                    // inject `shared pools` node as hint
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("${path}_pools")) }
            }

            // Construct schema cells
            root.node("cells").childrenList().forEach { childNode ->
                val id = childNode.node("id").requireKt<String>()
                val cell = run {
                    val coreNode = constructNode("core", childNode)
                    val curseNode = constructNode("curse", childNode)
                    SchemaCellFactory.schemaOf(childNode, coreNode, curseNode)
                }

                this += id to cell // add it to the result map
            }
        }

        return NekoItemImpl(key, uuid, provider, material, effectiveSlot, schemaMeta, schemaCell, behaviors)
    }
}

private inline fun <reified T : SchemaItemMeta<*>> ImmutableClassToInstanceMap.Builder<SchemaItemMeta<*>>.loadAndSave(
    node: ConfigurationNode, key: Key,
) = loadAndSave<T>(node, key.value())

private inline fun <reified T : SchemaItemMeta<*>> ImmutableClassToInstanceMap.Builder<SchemaItemMeta<*>>.loadAndSave(
    node: ConfigurationNode, vararg path: String,
) {
    val schemaItemMeta = node.node(*path).requireKt<T>()
    this.put(T::class.java, schemaItemMeta)
}