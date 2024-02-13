package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.item.scheme.NekoItemFactory
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object NekoItemRegistry : KoinComponent, Initializable, Reloadable,
    Registry<Key, NekoItem> by HashMapRegistry() {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    // configuration stuff
    private val dataDir: File by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_CONFIG_DIR) }

    fun get(key: String): NekoItem? = get(Key.key(key))
    fun getOrThrow(key: String): NekoItem = getOrThrow(Key.key(key))

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearName2Object()

        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDir.walk().maxDepth(1)
            .drop(1) // exclude the `dataDir` itself
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it
                logger.info("Loaded namespace: {}", it)
            }

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER)) // will be reused

        // then walk each file (i.e., each item) in each namespace
        namespaceDirs.forEach { namespaceDir ->
            namespaceDir.walk()
                .drop(1) // exclude the namespace directory itself
                .forEach { itemFile ->
                    val text = itemFile.bufferedReader().use { it.readText() }
                    val node = loaderBuilder.buildAndLoadString(text)

                    val namespace = namespaceDir.name
                    val value = itemFile.nameWithoutExtension

                    val key = Key.key(namespace, value)
                    val item = NekoItemFactory.create(key, node)

                    registerName2Object(key, item)
                    logger.info("Loaded item: {}", key)
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