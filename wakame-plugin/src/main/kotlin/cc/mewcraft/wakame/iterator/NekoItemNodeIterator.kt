package cc.mewcraft.wakame.iterator

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.registry.ITEM_CONFIG_DIR
import cc.mewcraft.wakame.registry.ITEM_CONFIG_LOADER
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object NekoItemNodeIterator : KoinComponent {
    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    // configuration stuff
    private val dataDir: File by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_CONFIG_DIR) }

    /**
     * 这里的 [block] 会被多次调用
     * 每次调用时会传入一个配置文件的 [Key] 和一个访问配置文件的根 [ConfigurationNode]。
     */
    fun execute(block: (Key, ConfigurationNode) -> Unit) {
        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDir.walk().maxDepth(1)
            .drop(1) // exclude the `dataDir` itself
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it.also { logger.info("Loading namespace: {}", it.name) }
            }

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER)) // will be reused

        // then walk each file (i.e., each item) in each namespace
        namespaceDirs.forEach { namespaceDir ->
            namespaceDir.walk()
                .drop(1) // exclude the namespace directory itself
                .filter { it.extension == "yml" }
                .forEach { itemFile ->
                    val namespace = namespaceDir.name
                    val value = itemFile.nameWithoutExtension
                    val key = Key.key(namespace, value).also {
                        logger.info("Loading item: {}", it)
                    }

                    val text = itemFile.bufferedReader().use { it.readText() }
                    val node = loaderBuilder.buildAndLoadString(text)
                    block(key, node)
                }
        }
    }
}