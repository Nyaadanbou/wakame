package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.item.EffectiveSlot
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
import kotlin.collections.set

object NekoItemFactory {
    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param root the configuration node holding the data of the item
     * @param relPath the relative path of the item in the configuration
     * @return a new [NekoItem]
     */
    fun create(key: Key, root: ConfigurationNode, relPath: Path): NekoItem {
        val provider = NodeConfigProvider(root, relPath.toString())

        // Deserialize basic data
        val uuid = root.node("uuid").requireKt<UUID>()
        val material = root.node("material").requireKt<Key>()
        val effectiveSlot = root.node("effective_slot").requireKt<EffectiveSlot>()

        // Deserialize item behaviors
        val behaviors: List<String> = root.node("behaviors").childrenMap()
            .mapNotNull { (key, _) -> key?.toString() }

        // Deserialize standalone item meta
        val schemaMeta = ImmutableClassToInstanceMap.builder<SchemaItemMeta<*>>().apply {
            // Side note 1: buildMap preserves the insertion order

            // Side note 2: always put all schema metadata for a `NekoItem`
            // even if the schema metadata contains "nothing".

            // Side note 3: whether the data will be written to the item's NBT
            // is decided by the item stack generation process, not here.

            // (by alphabet order, in case you miss something)
            loadAndSave<SDisplayLoreMeta>(root, "lore")
            loadAndSave<SDisplayNameMeta>(root, "display_name")
            loadAndSave<SDurabilityMeta>(root, "durability")
            loadAndSave<SElementMeta>(root, "elements")
            loadAndSave<SKizamiMeta>(root, "kizami")
            loadAndSave<SLevelMeta>(root, "level")
            loadAndSave<SRarityMeta>(root, "rarity")
            loadAndSave<SSkinMeta>(root, "skin")
            loadAndSave<SSkinOwnerMeta>(root, "skin_owner")
        }.build()

        // Deserialize item cells
        val schemaCell: Map<String, SchemaCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            root.node("cells").childrenList().forEach { n ->
                val id = n.node("id").requireKt<String>()
                val coreNode = n.node("core").string
                    ?.let { root.node("core_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("core_pools")) } // inject `shared pools` node as hint
                val curseNode = n.node("curse").string
                    ?.let { root.node("curse_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("curse_pools")) } // ^ same
                val cell = SchemaCellFactory.schemaOf(n, coreNode, curseNode)
                this[id] = cell
            }
        }

        val ret = NekoItemImpl(key, uuid, provider, material, effectiveSlot, schemaMeta, schemaCell, behaviors)
        return ret
    }
}

private inline fun <reified T : SchemaItemMeta<*>> ImmutableClassToInstanceMap.Builder<SchemaItemMeta<*>>.loadAndSave(
    node: ConfigurationNode,
    vararg path: String,
) {
    val schemaItemMeta = node.node(*path).requireKt<T>()
    this.put(T::class.java, schemaItemMeta)
}