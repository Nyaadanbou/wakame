package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.cell.SchemeCellFactory
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.util.typedRequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID


object WakaItemFactory {
    /**
     * Creates a [WakaItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param node the configuration node holding the data of the item
     * @return a new [WakaItem]
     */
    fun create(key: Key, node: ConfigurationNode): WakaItem {
        val uuid = node.node("uuid").typedRequire<UUID>()

        // region Read item meta
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
        // endregion

        // region Read item cells
        val schemeCells: Map<String, SchemeCell> = buildMap {
            // Side note: buildMap preserves the insertion order

            node.node("cells").childrenList().forEach { n ->
                val cellId = n.node("id").typedRequire<String>()
                val coreNode = n.node("core").string?.let { groupId -> node.node("cell_groups", groupId) }
                val curseNode = n.node("curse").string?.let { groupId -> node.node("curse_groups", groupId) }
                val schemeCell = SchemeCellFactory.schemeOf(n, coreNode, curseNode)

                put(cellId, schemeCell)
            }
        }
        // endregion

        val ret = WakaItemImpl(key, uuid, schemeMeta, schemeCells)
        return ret
    }

    private inline fun <reified T : SchemeMeta<*>> MutableMap<Key, SchemeMeta<*>>.loadAndPutMeta(
        node: ConfigurationNode,
        vararg path: Any,
    ) {
        // always put all metadata for all `NekoItem`s
        // even if the metadata contains "nothing".

        // whether the data will be actually put on the item's NBT or not
        // is decided by the item stack generation process, not here.

        put(SchemeMetaKeys.get<T>(), node.node(path).typedRequire<T>())
    }
}