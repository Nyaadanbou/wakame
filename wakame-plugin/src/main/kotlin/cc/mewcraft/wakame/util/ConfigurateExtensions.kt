package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.config.configurate.ComponentSerializer
import cc.mewcraft.wakame.config.configurate.IntRangeSerializer
import cc.mewcraft.wakame.config.configurate.KeySerializer
import cc.mewcraft.wakame.config.configurate.StyleBuilderApplicableSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal typealias NekoConfigurationLoader = YamlConfigurationLoader

/**
 * Apply common settings for the [YamlConfigurationLoader.Builder].
 */
internal fun YamlConfigurationLoader.Builder.withDefaultSettings(): YamlConfigurationLoader.Builder {
    return this
        // use 2 spaces indent
        .indent(2)
        // always use block style
        .nodeStyle(NodeStyle.BLOCK)
        // register common serializers
        .defaultOptions { options ->
            options
                // don't automatically write default values from serializers back to config files
                .shouldCopyDefaults(false)
                // add common serializers
                .serializers {
                    it.kregister(KeySerializer)
                    it.kregister(IntRangeSerializer)
                    it.kregister(NumericValueSerializer)
                    it.register(ComponentSerializer)
                    it.register(StyleBuilderApplicableSerializer)
                }
        }
}

/**
 * Creates a basic builder of configuration loader.
 */
internal fun buildYamlLoader(
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader.Builder {
    return YamlConfigurationLoader.builder()
        .withDefaultSettings()
        .defaultOptions { options ->
            options.serializers {
                it.builder()
            }
        }
}

/**
 * @see TypeSerializerCollection.Builder.register
 */
internal inline fun <reified T> TypeSerializerCollection.Builder.kregister(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder {
    return this.register({ javaTypeOf<T>() == it }, serializer)
}

/**
 * @see ConfigurationNode.require
 */
internal inline fun <reified T> ConfigurationNode.krequire(): T {
    return this.krequire(typeOf<T>())
}

/**
 * @see ConfigurationNode.require
 */
internal fun <T : Any> ConfigurationNode.krequire(clazz: KClass<T>): T {
    val ret = this.get(clazz) ?: throw NoSuchElementException(
        "Missing value of type '${clazz}' at '${path().joinToString(".")}'"
    )
    return ret
}

/**
 * @see ConfigurationNode.require
 */
internal fun <T> ConfigurationNode.krequire(type: KType): T {
    val ret = this.get(type) ?: throw NoSuchElementException(
        "Missing value of type '${type}' at '${path().joinToString(".")}'"
    )
    @Suppress("UNCHECKED_CAST")
    return ret as T
}
