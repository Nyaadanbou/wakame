package cc.mewcraft.wakame.iterator

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.NamespacedPathCollector
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path

/**
 * NekoItem 物品配置文件的遍历器.
 *
 * 该遍历器会实时反应文件的变化.
 */
internal object NekoItemNodeIterator : Iterable<Triple<Key, Path, ConfigurationNode>>, KoinComponent {
    override fun iterator(): Iterator<Triple<Key, Path, ConfigurationNode>> {
        return collectElements().iterator()
    }

    private fun collectElements(): List<Triple<Key, Path, ConfigurationNode>> {
        val mutableList = mutableListOf<Triple<Key, Path, ConfigurationNode>>()
        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_PROTO_CONFIG_DIR)
        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) // will be reused

        val itemFileCollector = NamespacedPathCollector(dataDirectory, true)
        itemFileCollector.collect("yml").forEach { namespacedPath ->
            val namespace = namespacedPath.namespace
            val value = namespacedPath.path
            val key = Key(namespace, value)

            val text = namespacedPath.file.readText()
            val path = namespacedPath.file.toPath()
            val node = loaderBuilder.buildAndLoadString(text)
            val triple = Triple(key, path, node)
            mutableList.add(triple)
        }

        return mutableList
    }
}