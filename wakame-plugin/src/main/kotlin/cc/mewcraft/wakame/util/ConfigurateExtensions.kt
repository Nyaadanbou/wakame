package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KOISH_CONFIGURATE_SERIALIZERS
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.BufferedWriter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


// Builder extensions


internal fun buildYamlConfigLoader(block: YamlConfigLoaderDSL.() -> Unit): YamlConfigurationLoader.Builder {
    return YamlConfigLoaderDSL().apply(block).builder
}

internal fun buildGsonConfigLoader(block: GsonConfigLoaderDSL.() -> Unit): GsonConfigurationLoader.Builder {
    return GsonConfigLoaderDSL().apply(block).builder
}

internal class YamlConfigLoaderDSL {
    /* private */ val builder: YamlConfigurationLoader.Builder = YamlConfigurationLoader.builder()

    // 应用默认设置的逻辑
    fun withDefaults(): YamlConfigLoaderDSL {
        builder.withDefaultEverything()
        return this
    }

    // 注册序列化器的逻辑
    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): YamlConfigLoaderDSL {
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    // 定义数据来源的逻辑
    fun source(reader: () -> BufferedReader): YamlConfigLoaderDSL {
        builder.source(reader)
        return this
    }

    // 定义数据去向的逻辑
    fun sink(writer: () -> BufferedWriter): YamlConfigLoaderDSL {
        builder.sink(writer)
        return this
    }

    fun build(): YamlConfigurationLoader {
        return builder.build()
    }
}

internal class GsonConfigLoaderDSL {
    /* private */ val builder: GsonConfigurationLoader.Builder = GsonConfigurationLoader.builder()

    fun withDefaults(): GsonConfigLoaderDSL {
        // TODO GsonConfig 默认的设置 (可能也不需要?)
        return this
    }

    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): GsonConfigLoaderDSL {
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    fun source(reader: () -> BufferedReader): GsonConfigLoaderDSL {
        builder.source(reader)
        return this
    }

    fun sink(writer: () -> BufferedWriter): GsonConfigLoaderDSL {
        builder.sink(writer)
        return this
    }

    fun build(): GsonConfigurationLoader {
        return builder.build()
    }
}

internal fun YamlConfigurationLoader.Builder.withDefaultYamlConfigs(): YamlConfigurationLoader.Builder {
    return apply {
        indent(2) // use 2 spaces indent
        nodeStyle(NodeStyle.BLOCK) // always use block style
    }
}

internal fun YamlConfigurationLoader.Builder.withDefaultConfigOptions(): YamlConfigurationLoader.Builder {
    return defaultOptions { options ->
        options.shouldCopyDefaults(false) // don't automatically write default values from serializers back to config files
            .implicitInitialization(true) // enable implicit initialization
    }
}

internal fun YamlConfigurationLoader.Builder.withDefaultTypeSerializers(): YamlConfigurationLoader.Builder {
    return this.defaultOptions { options ->
        options.serializers { collection ->
            collection.registerAnnotatedObjects(ObjectMappers.DEFAULT)
            collection.registerAll(KOISH_CONFIGURATE_SERIALIZERS)
        }
    }
}

/**
 * Apply all default settings to the builder.
 */
internal fun YamlConfigurationLoader.Builder.withDefaultEverything(): YamlConfigurationLoader.Builder {
    return withDefaultYamlConfigs().withDefaultConfigOptions().withDefaultTypeSerializers()
}

/**
 * Creates a basic builder of configuration loader.
 */
internal fun buildYamlLoader(
    builder: TypeSerializerCollection.Builder.() -> Unit = {},
): YamlConfigurationLoader.Builder {
    return YamlConfigurationLoader.builder()
        .withDefaultEverything()
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


// Node extensions


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
        "Can't parse value of type '${clazz}' at '[${path().joinToString()}]'"
    )
    return ret
}

/**
 * @see ConfigurationNode.require
 */
internal fun <T> ConfigurationNode.krequire(type: KType): T {
    val ret = this.get(type) ?: throw NoSuchElementException(
        "Can't parse value of type '${type}' at '[${path().joinToString()}]'"
    )
    @Suppress("UNCHECKED_CAST")
    return ret as T
}
