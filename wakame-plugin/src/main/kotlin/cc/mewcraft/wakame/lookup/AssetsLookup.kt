package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@PreWorldDependency(
    runBefore = [
        ItemRegistry::class
    ]
)
@ReloadDependency(
    runBefore = [
        ItemRegistry::class
    ]
)
object AssetsLookup : Initializable, KoinComponent {
    // K - NekoItem key
    // V - Assets
    private val assets: Multimap<Key, Assets> = MultimapBuilder
        .hashKeys()
        .treeSetValues<Assets> { o1, o2 -> o1.variant.compareTo(o2.variant) }
        .build()

    private val plugin: WakamePlugin by inject()

    private fun loadConfiguration() {
        assets.clear()
        plugin.saveResourceRecursively(PLUGIN_ASSETS_DIR)

        NekoItemNodeIterator.forEach { (key, _, root) ->
            val assetsNodes = root.node("assets").childrenList()
            for (assetsNode in assetsNodes) {
                val sid = assetsNode.node("variant").krequire<Int>()
                val pathNode = assetsNode.node("path")
                val path = if (pathNode.rawScalar() != null) {
                    listOf(pathNode.krequire<String>())
                } else {
                    pathNode.krequire<List<String>>()
                }
                assets.put(key, ItemAssets(key, sid, path))
            }
        }
    }

    fun getAssets(key: Key): List<Assets> {
        require(ItemRegistry.CUSTOM.find(key) != null) { "No such NekoItem: $key" }
        return assets[key].toList()
    }

    fun getAssets(key: Key, sid: Int): Assets {
        require(ItemRegistry.CUSTOM.find(key) != null) { "No such NekoItem: $key" }
        return assets[key].firstOrNull { it.variant == sid } ?: throw NoSuchElementException("No such variant: $sid")
    }

    val allAssets: Collection<Assets>
        get() = assets.values()

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}