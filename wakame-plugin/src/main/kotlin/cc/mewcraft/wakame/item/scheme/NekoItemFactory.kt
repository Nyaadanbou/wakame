package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.cell.SchemeCellFactory
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.UUID


object NekoItemFactory : KoinComponent {
    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param root the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    fun create(key: Key, root: ConfigurationNode): NekoItem {
        val uuid = root.node("uuid").requireKt<UUID>()

        // Deserialize item meta
        val schemeItemMeta: Map<Key, SchemeItemMeta<*>> = buildMap {
            // Side note 1: buildMap preserves the insertion order

            // Side note 2: always put all metadata for all `NekoItem`s
            // even if the metadata contains "nothing".

            // Side note 3: whether the data will be put on the item's NBT
            // is decided by the item stack generation process, not here.

            // (by alphabet order, in case you miss something)
            loadAndSave<DisplayLoreMeta>(root, "lore")
            loadAndSave<DisplayNameMeta>(root, "display_name")
            loadAndSave<DurabilityMeta>(root, "durability")
            loadAndSave<ElementMeta>(root, "elements")
            loadAndSave<KizamiMeta>(root, "kizami")
            loadAndSave<LevelMeta>(root, "level")
            loadAndSave<MaterialMeta>(root, "material")
            loadAndSave<RarityMeta>(root, "rarity")
            loadAndSave<SkinMeta>(root, "skin")
            loadAndSave<SkinOwnerMeta>(root, "skin_owner")
        }

        // Deserialize item cells
        val schemeCells: Map<String, SchemeCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            root.node("cells").childrenList().forEach { n ->
                val id = n.node("id").requireKt<String>()

                val coreNode: ConfigurationNode? = n.node("core")
                    .string
                    ?.let { root.node("core_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("core_pools")) } // inject `shared pools` node as hint
                val curseNode: ConfigurationNode? = n.node("curse")
                    .string
                    ?.let { root.node("curse_groups", it) }
                    ?.also { it.hint(AbstractGroupSerializer.SHARED_POOLS, root.node("curse_pools")) } // ^ same

                val cell = SchemeCellFactory.schemeOf(n, coreNode, curseNode)

                this[id] = cell
            }
        }

        val ret = NekoItemImpl(key, uuid, schemeItemMeta, schemeCells)
        return ret
    }

    private inline fun <reified T : SchemeItemMeta<*>> MutableMap<Key, SchemeItemMeta<*>>.loadAndSave(
        node: ConfigurationNode,
        vararg path: String,
    ) {
        val deserialized = requireNotNull(node.node(*path).get<T>()) { "Can't deserialize item meta from path ${path.contentToString()}" }
        this[SchemeItemMetaKeys.get<T>()] = deserialized
    }
}