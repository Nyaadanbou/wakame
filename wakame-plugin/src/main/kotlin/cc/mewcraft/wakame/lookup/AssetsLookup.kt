package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR_NAME
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
    private val assets: Multimap<Key, ItemAssets> = MultimapBuilder
        .hashKeys()
        .treeSetValues<ItemAssets> { o1, o2 -> o1.variant.compareTo(o2.variant) }
        .build()

    private val plugin: WakamePlugin by inject()

    private fun loadConfiguration() {
        assets.clear()
        plugin.saveResourceRecursively(PLUGIN_ASSETS_DIR_NAME)

        NekoItemNodeIterator.forEach { (key, _, root) ->
            val assetsNodes = root.node("assets").childrenList()
            for (assetsNode in assetsNodes) {
                val variant = assetsNode.node("variant").krequire<Int>()
                val path = with(assetsNode.node("path")) {
                    if (rawScalar() != null) {
                        listOf(krequire<String>())
                    } else {
                        krequire<List<String>>()
                    }
                }

                assets.put(key, ItemAssets(key, variant, path))
            }
        }
    }

    val allAssets: Collection<ItemAssets>
        get() = assets.values()

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}