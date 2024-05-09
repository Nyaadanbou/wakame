package cc.mewcraft.wakame.iterator

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path

/**
 * NekoItem 物品配置文件的遍历器。
 */
object NekoItemNodeIterator : KoinComponent {
    private val LOGGER: Logger by inject()

    /**
     * 根据配置文件的目录结构，遍历所有物品配置文件，并对每个物品配置文件执行 [block]。
     * 每次调用时会传入一个物品的 [Key] 和对应文件的根 [ConfigurationNode]。
     *
     * @param block 需要执行的代码块，会被多次调用。
     * - 参数 [Key] 为物品的唯一标识
     * - 参数 [ConfigurationNode] 为物品的根配置
     * - 参数 [Path] 为物品配置文件的路径
     */
    fun forEach(block: (Key, Path, ConfigurationNode) -> Unit) {
        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_PROTO_CONFIG_DIR)
        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it.also {
                    LOGGER.info("Loading namespace: {}", it.name)
                }
            }

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) // will be reused

        // then walk each file (i.e., each item) in each namespace
        namespaceDirs.forEach { namespaceDir ->
            namespaceDir.walk()
                .drop(1) // exclude the namespace directory itself
                .filter { it.extension == "yml" }
                .forEach { itemFile ->
                    val namespace = namespaceDir.name
                    val value = itemFile.nameWithoutExtension
                    val key = Key(namespace, value).also {
                        LOGGER.info("Loading item: {}", it)
                    }

                    val text = itemFile.bufferedReader().use { it.readText() }
                    val path = itemFile.toPath()
                    val node = loaderBuilder.buildAndLoadString(text)
                    block(key, path, node)
                }
        }
    }
}