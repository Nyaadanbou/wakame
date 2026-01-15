package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.common.config.PluginConfigs
import net.kyori.adventure.key.Key
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

class PaperPluginConfigs(
    private val plugin: JavaPlugin,
) : PluginConfigs() {

    override fun defaultNamespace(): String {
        return PLUGIN_NAMESPACE
    }

    override fun extractDefaultFiles() {
        plugin.saveDefaultConfig()
    }

    override fun resolvePath(configId: Key): Path {
        return when (configId.namespace()) {
            PLUGIN_NAMESPACE -> plugin.dataPath.resolve("${configId.value()}.yml")
            else -> throw IllegalArgumentException("Unknown config namespace: ${configId.namespace()}")
        }
    }
}