package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.initializer.Initializer
import org.spongepowered.configurate.ConfigurationNode

/**
 * Lazily gets specific value from the **main configuration**, a.k.a. the "config.yml".
 *
 * @param path the path to the config node
 * @param transform the transformation of the config node
 * @return the deserialized value
 */
fun <T> config(vararg path: String, transform: ConfigurationNode.() -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) { Initializer.CONFIG.node(*path).transform() }
}