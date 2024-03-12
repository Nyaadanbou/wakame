package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@PreWorldDependency(
    runBefore = [
        NekoItemRegistry::class
    ]
)
@ReloadDependency(
    runBefore = [
        NekoItemRegistry::class
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

        NekoItemNodeIterator.execute { key, root ->
            val assetsNodes = root.node("assets").childrenList()
            for (assetsNode in assetsNodes) {
                val sid = assetsNode.node("variant").requireKt<Int>()
                val pathNode = assetsNode.node("path")
                val path = if (pathNode.rawScalar() != null) {
                    listOf(pathNode.requireKt<String>())
                } else {
                    pathNode.requireKt<List<String>>()
                }
                assets.put(key, ItemAssets(key, sid, path))
            }
        }
    }

    fun getAssets(key: Key): List<Assets> {
        require(NekoItemRegistry.get(key) != null) { "No such NekoItem: $key" }
        return assets[key].toList()
    }

    fun getAssets(key: Key, sid: Int): Assets {
        require(NekoItemRegistry.get(key) != null) { "No such NekoItem: $key" }
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