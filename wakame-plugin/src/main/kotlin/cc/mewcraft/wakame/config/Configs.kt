package cc.mewcraft.wakame.config

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.ConfigsBase
import cc.mewcraft.lazyconfig.configurate.STANDARD_SERIALIZERS
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.configurate.KOISH_SERIALIZERS
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import kotlin.io.path.exists

internal object Configs : ConfigsBase() {

    internal fun initialize() {
        // 注册 ConfigAccess 实例
        ConfigAccess.setImplementation(this)

        // 先提取必要的文件到插件数据目录, 否则接下来 reload 会读取到空文件
        ConfigsExtractor.extractDefaults()

        // 更新一开始的特殊值为当前时间戳
        lastReloadTimestamp = System.currentTimeMillis()

        // 重新读取已经存在的实例
        configProviders.values.asSequence()
            .filter { it.path.exists() }
            .forEach { it.reload() }

        registerReload { _ -> Bukkit.getOnlinePlayers().forEach(Player::updateInventory) }
    }

    internal fun cleanup() {
        configProviders.clear()
    }

    override fun resolveConfigPath(configId: Key): Path {
        val dataFolder = when (configId.namespace()) {
            KOISH_NAMESPACE -> KoishDataPaths.ROOT // -> plugins/<data_folder>
            else -> throw IllegalArgumentException("Only 'koish' namespace is currently supported.")
        }
        return dataFolder.resolve("configs").resolve(configId.value() + ".yml")
    }

    override fun createBuilder(namespace: String): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader.builder()
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { opts ->
                opts.serializers(buildSerializers(namespace))
            }
    }

    override fun buildSerializers(namespace: String): TypeSerializerCollection {
        return TypeSerializerCollection.builder().apply {
            customSerializers[namespace]?.build()?.let(::registerAll)
            registerAll(STANDARD_SERIALIZERS)
            registerAll(KOISH_SERIALIZERS)
        }.build()
    }
}
