package cc.mewcraft.wakame.util

import cc.mewcraft.spatula.utils.RangeParser
import com.google.common.base.Throwables
import com.google.common.collect.Range
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

/**
 * Apply common settings for the [YamlConfigurationLoader.Builder].
 */
internal fun YamlConfigurationLoader.Builder.applyCommons(): YamlConfigurationLoader.Builder {
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
                    it.registerKt(KeySerializer())
                    it.registerKt(IntRangeParser())
                    it.registerKt(NumericValueSerializer())
                }
        }
}

/**
 * Creates a basic builder of configuration loader.
 */
internal fun buildBasicConfigurationLoader(
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader.Builder {
    return YamlConfigurationLoader.builder()
        .applyCommons()
        .defaultOptions { options ->
            options.serializers {
                it.builder()
            }
        }
}

/**
 * @see TypeSerializerCollection.Builder.register
 */
internal inline fun <reified T> TypeSerializerCollection.Builder.registerKt(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    register({ javaTypeOf<T>() == it }, serializer)

/**
 * @see ConfigurationNode.require
 */
internal inline fun <reified T> ConfigurationNode.requireKt(): T {
    val ret = this.get(javaTypeOf<T>())
        ?: throw NoSuchElementException(
            "Missing value of type '${T::class.simpleName}' at '${path().joinToString(" -> ")}'"
        )
    return ret as T
}

//<editor-fold desc="Common Serializers">
/**
 * The deserializer for [Key].
 */
internal class KeySerializer : TypeSerializer<Key> {
    override fun deserialize(type: Type, node: ConfigurationNode): Key = Key.key(node.requireKt<String>())
    override fun serialize(type: Type?, obj: Key?, node: ConfigurationNode?): Nothing = throw UnsupportedOperationException()
}

/**
 * The deserializer for [IntRange][Range].
 */
internal class IntRangeParser : TypeSerializer<Range<Int>> {
    override fun deserialize(type: Type, node: ConfigurationNode): Range<Int> = RangeParser.parseIntRange(node.requireKt<String>())
    override fun serialize(type: Type, obj: Range<Int>?, node: ConfigurationNode): Nothing = throw UnsupportedOperationException()
}
//</editor-fold>