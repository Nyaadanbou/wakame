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
import org.slf4j.Logger

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
    private val plugin: WakamePlugin by inject()
    private val logger: Logger by inject()

    // K - NekoItem key
    // V - Assets
    private val assets: Multimap<Key, ItemAssets> = MultimapBuilder
        .hashKeys()
        .arrayListValues()
        .build()

    private fun loadConfiguration() {
        assets.clear()
        plugin.saveResourceRecursively(PLUGIN_ASSETS_DIR_NAME)

        NekoItemNodeIterator.forEach { (key, _, root) ->
            val assetsNodes = root.node("assets").childrenList()
            for (assetsNode in assetsNodes) {
                try {
                    val path = assetsNode.node("path").krequire<String>()
                    val modelFile = AssetUtils.getFileOrThrow(path, "json")
                    assets.put(key, ItemAssets(key, modelFile))
                } catch (_: Exception) {
                    logger.warn("在读取物品 $key 的材质时出现错误")
                }
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