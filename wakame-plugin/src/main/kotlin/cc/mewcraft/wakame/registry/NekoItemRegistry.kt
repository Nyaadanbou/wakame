package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.WakaItem
import cc.mewcraft.wakame.item.scheme.WakaItemFactory
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object NekoItemRegistry : KoinComponent, Initializable, Reloadable,
    Registry<Key, WakaItem> by HashMapRegistry() {

    private val logger: Logger by inject()

    // configuration stuff
    private val dataDir: File by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_CONFIG_DIR) }

    fun get(key: String): WakaItem? = get(Key.key(key))
    fun getOrThrow(key: String): WakaItem = getOrThrow(Key.key(key))

    private fun loadConfiguration() {
        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDir.walk().maxDepth(1)
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it
                logger.info("Loaded namespace dir: {}", it)
            }

        // then walk each file (i.e., each item) in each namespace
        namespaceDirs.forEach { namespaceDir ->
            namespaceDir.walk().forEach { itemFile ->
                val builder = get<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER))
                val node = builder.buildAndLoadString(itemFile.bufferedReader().use { it.readText() })
                val key = Key.key(namespaceDir.name.lowercase(), itemFile.name.lowercase())
                val item = WakaItemFactory.create(key, node)

                registerName2Object(key, item)
                logger.info("Loaded item file: {}", key)
            }
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}