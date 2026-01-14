package cc.mewcraft.wakame.config

import cc.mewcraft.lazyconfig.access.ConfigsBase
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.configurate.KOISH_SERIALIZERS
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.annotations.VisibleForTesting
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path

internal object Configs : ConfigsBase() {

    @VisibleForTesting
    internal fun cleanup() {
        configProviders.clear()
    }

    override fun defaultNamespace(): String {
        return KOISH_NAMESPACE
    }

    override fun extractDefaultFiles() {
        ConfigsExtractor.extractDefaults()
    }

    override fun resolvePath(configId: Key): Path {
        val dataFolder = when (configId.namespace()) {
            KOISH_NAMESPACE -> KoishDataPaths.ROOT // -> plugins/<data_folder>
            else -> throw IllegalArgumentException("Only $KOISH_NAMESPACE namespace is currently supported!")
        }
        val configPath = dataFolder.resolve("configs").resolve(configId.value() + ".yml")

        return configPath
    }

    override fun buildSerials(namespace: String): TypeSerializerCollection {
        val children = super.buildSerials(namespace)
            .childBuilder()
            .registerAll(KOISH_SERIALIZERS)
            .build()
        return children
    }

    override fun afterReload(keys: List<Key>) {
        Bukkit.getOnlinePlayers().forEach(Player::updateInventory)
    }
}
