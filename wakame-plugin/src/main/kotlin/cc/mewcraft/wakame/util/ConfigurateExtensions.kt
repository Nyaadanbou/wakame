package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

internal typealias NekoConfigurationLoader = ConfigurationLoader<CommentedConfigurationNode>
internal typealias NekoConfigurationNode = CommentedConfigurationNode

internal fun YamlConfigurationLoader.Builder.applyCommons(): YamlConfigurationLoader.Builder {
    return this.indent(2).nodeStyle(NodeStyle.BLOCK).defaultOptions { options ->
        options.serializers {
            it.typedRegister<Key>(KeySerializer())
        }
    }
}

internal inline fun <reified T> TypeSerializerCollection.Builder.typedRegister(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    register(typeTokenOf<T>(), serializer)

internal inline fun <reified T> ConfigurationNode.typedRequire(): T =
    require(typeOf<T>().javaType) as T

internal class KeySerializer : TypeSerializer<Key> {
    override fun deserialize(type: Type, node: ConfigurationNode): Key = Key.key(node.typedRequire<String>())
    override fun serialize(type: Type?, obj: Key?, node: ConfigurationNode?): Nothing = throw UnsupportedOperationException()
}