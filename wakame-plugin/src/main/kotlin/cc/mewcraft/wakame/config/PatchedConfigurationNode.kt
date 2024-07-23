package cc.mewcraft.wakame.config

import org.spongepowered.configurate.ConfigurationNode

data class PatchedConfigurationNode(
    val patch: ConfigurationNode,
    val default: ConfigurationNode
) {
    fun node(vararg path: Any?): ConfigurationNode {
        return if (patch.node(*path).virtual()) {
            default.node(*path)
        } else {
            patch.node(*path)
        }
    }

    fun node(path: MutableIterable<*>?): ConfigurationNode {
        return if (patch.node(path).virtual()) {
            default.node(path)
        } else {
            patch.node(path)
        }
    }
}