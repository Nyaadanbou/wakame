@file:JvmName("Builders")

package cc.mewcraft.wakame.util.configurate

import cc.mewcraft.lazyconfig.configurate.STANDARD_SERIALIZERS
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.BufferedWriter


fun yamlLoader(block: YamlLoaderDsl.() -> Unit): YamlConfigurationLoader.Builder {
    return YamlLoaderDsl().apply(block).builder
}

fun gsonLoader(block: GsonLoaderDsl.() -> Unit): GsonConfigurationLoader.Builder {
    return GsonLoaderDsl().apply(block).builder
}

class YamlLoaderDsl {
    internal val builder: YamlConfigurationLoader.Builder = YamlConfigurationLoader.builder()

    // 应用默认设置的逻辑
    fun withDefaults(): YamlLoaderDsl {
        builder.withDefaultEverything()
        return this
    }

    // 注册序列化器的逻辑
    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): YamlLoaderDsl {
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    // 定义数据来源的逻辑
    fun source(reader: () -> BufferedReader): YamlLoaderDsl {
        builder.source(reader)
        return this
    }

    // 定义数据去向的逻辑
    fun sink(writer: () -> BufferedWriter): YamlLoaderDsl {
        builder.sink(writer)
        return this
    }

    fun build(): YamlConfigurationLoader {
        return builder.build()
    }
}

class GsonLoaderDsl {
    internal val builder: GsonConfigurationLoader.Builder = GsonConfigurationLoader.builder()

    fun withDefaults(): GsonLoaderDsl {
        return this
    }

    fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): GsonLoaderDsl {
        builder.defaultOptions { options -> options.serializers(block) }
        return this
    }

    fun source(reader: () -> BufferedReader): GsonLoaderDsl {
        builder.source(reader)
        return this
    }

    fun sink(writer: () -> BufferedWriter): GsonLoaderDsl {
        builder.sink(writer)
        return this
    }

    fun build(): GsonConfigurationLoader {
        return builder.build()
    }
}

fun YamlConfigurationLoader.Builder.withDefaultYamlConfigs(): YamlConfigurationLoader.Builder {
    return apply {
        indent(2) // use 2 spaces indent
        nodeStyle(NodeStyle.BLOCK) // always use block style
    }
}

fun YamlConfigurationLoader.Builder.withDefaultConfigOptions(): YamlConfigurationLoader.Builder {
    return defaultOptions { options ->
        options.shouldCopyDefaults(false) // don't automatically write default values from serializers back to config files
            .implicitInitialization(true) // enable implicit initialization
    }
}

private val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    .registerAll(KOISH_SERIALIZERS)
    .registerAll(STANDARD_SERIALIZERS)
    .build()

fun YamlConfigurationLoader.Builder.withDefaultSerializers(): YamlConfigurationLoader.Builder {
    return defaultOptions { options ->
        options.serializers(SERIALIZERS)
    }
}

fun YamlConfigurationLoader.Builder.withDefaultEverything(): YamlConfigurationLoader.Builder {
    return withDefaultYamlConfigs().withDefaultConfigOptions().withDefaultSerializers()
}
