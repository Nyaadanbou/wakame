package cc.mewcraft.extracontexts.velocity

import cc.mewcraft.extracontexts.common.config.PluginConfigs
import net.kyori.adventure.key.Key
import java.nio.file.Path

class VelocityPluginConfigs(
    private val plugin: ExtraContextsVelocityPlugin,
) : PluginConfigs() {

    override fun defaultNamespace(): String {
        return PLUGIN_NAMESPACE
    }

    override fun extractDefaultFiles() {
        plugin.saveResource("config.yml")
    }

    override fun resolvePath(configId: Key): Path {
        return when (configId.namespace()) {
            PLUGIN_NAMESPACE -> plugin.dataDirectory.resolve("${configId.value()}.yml")
            else -> throw IllegalArgumentException("Unknown config namespace: ${configId.namespace()}")
        }
    }
}