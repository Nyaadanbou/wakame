package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.cell.SchemeCellFactory
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.ImmutableClassToInstanceMap
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID
import kotlin.collections.set

object NekoItemFactory {
    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param root the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    fun create(key: Key, root: ConfigurationNode): NekoItem {
        val uuid = root.node("uuid").requireKt<UUID>()
        val material = root.node("material").requireKt<Key>()
        val effectiveSlot = root.node("effective_slot").requireKt<EffectiveSlot>()

        // Deserialize item meta
        val schemeMeta = ImmutableClassToInstanceMap.builder<SchemeItemMeta<*>>().apply {
            // Side note 1: buildMap preserves the insertion order

            // Side note 2: always put all schema metadata for a `NekoItem`
            // even if the schema metadata contains "nothing".

            // Side note 3: whether the data will be written to the item's NBT
            // is decided by the item stack generation process, not here.

            // (by alphabet order, in case you miss something)
            loadAndSave<DisplayLoreMeta>(root, "lore")
            loadAndSave<DisplayNameMeta>(root, "display_name")
            loadAndSave<DurabilityMeta>(root, "durability")
            loadAndSave<ElementMeta>(root, "elements")
            loadAndSave<KizamiMeta>(root, "kizami")
            loadAndSave<LevelMeta>(root, "level")
            loadAndSave<RarityMeta>(root, "rarity")
            loadAndSave<SkinMeta>(root, "skin")
            loadAndSave<SkinOwnerMeta>(root, "skin_owner")
        }.build()

        // Deserialize item cells
        val schemeCell: Map<String, SchemeCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            root.node("cells").childrenList().forEach { n ->
                val id = n.node("id").requireKt<String>()
                val coreNode = n.node("core").string
                    ?.let { root.node("core_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("core_pools")) } // inject `shared pools` node as hint
                val curseNode = n.node("curse").string
                    ?.let { root.node("curse_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("curse_pools")) } // ^ same
                val cell = SchemeCellFactory.schemeOf(n, coreNode, curseNode)
                this[id] = cell
            }
        }

        val ret = NekoItemImpl(key, uuid, material, effectiveSlot, schemeMeta, schemeCell)
        return ret
    }

    private inline fun <reified T : SchemeItemMeta<*>> ImmutableClassToInstanceMap.Builder<SchemeItemMeta<*>>.loadAndSave(
        node: ConfigurationNode,
        vararg path: String,
    ) {
        val schemaItemMeta = node.node(*path).requireKt<T>()
        this.put(T::class.java, schemaItemMeta)
    }
}