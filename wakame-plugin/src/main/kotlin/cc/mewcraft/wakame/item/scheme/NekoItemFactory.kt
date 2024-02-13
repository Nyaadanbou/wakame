package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.cell.SchemeCellFactory
import cc.mewcraft.wakame.item.scheme.meta.*
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
     * @param node the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    fun create(key: Key, node: ConfigurationNode): NekoItem {
        val uuid = node.node("uuid").requireKt<UUID>()

        // Deserialize item meta
        val schemeMeta: Map<Key, SchemeMeta<*>> = buildMap {
            // Side note: buildMap preserves the insertion order

            // (by alphabet order)
            loadAndPutMeta<DisplayNameMeta>(node, "display_name")
            loadAndPutMeta<ElementMeta>(node, "elements")
            loadAndPutMeta<KizamiMeta>(node, "kizami")
            loadAndPutMeta<LevelMeta>(node, "level")
            loadAndPutMeta<LoreMeta>(node, "lore")
            loadAndPutMeta<MaterialMeta>(node, "material")
            loadAndPutMeta<RarityMeta>(node, "rarity")
            loadAndPutMeta<SkinMeta>(node, "skin")
            loadAndPutMeta<SkinOwnerMeta>(node, "skin_owner")
        }

        // Deserialize item cells
        val schemeCells: Map<String, SchemeCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            node.node("cells").childrenList().forEach { n ->
                val id = n.node("id").requireKt<String>()
                val coreN = n.node("core").string?.let { groupId -> node.node("core_selectors", groupId) }
                val curseN = n.node("curse").string?.let { groupId -> node.node("curse_selectors", groupId) }
                val cell = SchemeCellFactory.schemeOf(n, coreN, curseN)

                put(id, cell)
            }
        }

        val ret = NekoItemImpl(key, uuid, schemeMeta, schemeCells)
        return ret
    }

    private inline fun <reified T : SchemeMeta<*>> MutableMap<Key, SchemeMeta<*>>.loadAndPutMeta(
        node: ConfigurationNode,
        vararg path: String,
    ) {
        // always put all metadata for all `NekoItem`s
        // even if the metadata contains "nothing".

        // whether the data will be ultimately put on the item's NBT
        // is decided by the item stack generation process, not here.

        val deserialized = requireNotNull(node.node(*path).get<T>()) { "Can't deserialize scheme meta from path ${path.contentToString()}" }
        put(SchemeMetaKeys.get<T>(), deserialized)
    }
}