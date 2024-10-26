package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.config.configurate.*
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.BufferedWriter
import kotlin.reflect.*

internal typealias NekoConfigurationLoader = YamlConfigurationLoader


// Builder extensions


internal fun yamlConfig(block: YamlConfigDSL.() -> Unit): YamlConfigurationLoader.Builder {
    return YamlConfigDSL().apply(block).builder
}

internal fun gsonConfig(block: GsonConfigDSL.() -> Unit): GsonConfigurationLoader.Builder {
    return GsonConfigDSL().apply(block).builder
}

internal class YamlConfigDSL {
    /* private */ val builder: YamlConfigurationLoader.Builder = YamlConfigurationLoader.builder()

    fun withDefaults(): YamlConfigDSL {
        // 应用默认设置的逻辑
        builder.withDefaults()
        return this
    }

    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): YamlConfigDSL {
        // 注册序列化器的逻辑
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    fun source(reader: () -> BufferedReader): YamlConfigDSL {
        // 定义数据来源的逻辑
        builder.source(reader)
        return this
    }

    fun sink(writer: () -> BufferedWriter): YamlConfigDSL {
        // 定义数据去向的逻辑
        builder.sink(writer)
        return this
    }

    fun build(): YamlConfigurationLoader {
        return builder.build()
    }
}

internal class GsonConfigDSL {
    /* private */ val builder: GsonConfigurationLoader.Builder = GsonConfigurationLoader.builder()

    fun withDefaults(): GsonConfigDSL {
        // TODO GsonConfig 默认的设置 (可能也不需要?)
        return this
    }

    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): GsonConfigDSL {
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    fun source(reader: () -> BufferedReader): GsonConfigDSL {
        builder.source(reader)
        return this
    }

    fun sink(writer: () -> BufferedWriter): GsonConfigDSL {
        builder.sink(writer)
        return this
    }

    fun build(): GsonConfigurationLoader {
        return builder.build()
    }
}

/**
 * Apply common settings for the [YamlConfigurationLoader.Builder].
 */
internal fun YamlConfigurationLoader.Builder.withDefaults(): YamlConfigurationLoader.Builder {
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
                    it.register(StyleSerializer)
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
        .withDefaults()
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


// ConfigurationNode extensions


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
        "Can't get the value of type '${clazz}' at '${path().joinToString(".")}'"
    )
    return ret
}

/**
 * @see ConfigurationNode.require
 */
internal fun <T> ConfigurationNode.krequire(type: KType): T {
    val ret = this.get(type) ?: throw NoSuchElementException(
        "Can't get the value of type '${type}' at '${path().joinToString(".")}'"
    )
    @Suppress("UNCHECKED_CAST")
    return ret as T
}
