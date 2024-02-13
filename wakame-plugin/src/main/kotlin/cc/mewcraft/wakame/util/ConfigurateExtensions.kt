package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.lang.reflect.Type

internal typealias NekoConfigurationLoader = YamlConfigurationLoader
internal typealias NekoConfigurationNode = CommentedConfigurationNode

internal fun YamlConfigurationLoader.Builder.applyCommons(): YamlConfigurationLoader.Builder {
    return this
        // use 2 spaces indent
        .indent(2)
        // always use block style
        .nodeStyle(NodeStyle.BLOCK)
        // register common serializers
        .defaultOptions { options ->
            options
                // don't automatically write default values back to config files
                .shouldCopyDefaults(false)
                // add common serializers
                .serializers {
                    it.registerKt(KeySerializer())
                    it.registerKt(NumericValueSerializer())
                }
        }
}

internal inline fun <reified T> TypeSerializerCollection.Builder.registerKt(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder {
    register({ javaTypeOf<T>() == it }, serializer)
    return this
}

internal inline fun <reified T> ConfigurationNode.requireKt(): T {
    return require(javaTypeOf<T>()) as T
}

internal class KeySerializer : TypeSerializer<Key> {
    override fun deserialize(type: Type, node: ConfigurationNode): Key = Key.key(node.requireKt<String>())
    override fun serialize(type: Type?, obj: Key?, node: ConfigurationNode?): Nothing = throw UnsupportedOperationException()
}