package cc.mewcraft.wakame.util

import cc.mewcraft.spatula.utils.RangeParser
import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.context.GlobalContext
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.lang.reflect.Type
import java.util.function.Predicate

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
                    it.registerKt(KeySerializer)
                    it.registerKt(IntRangeParser)
                    it.registerKt(NumericValueSerializer)
                    it.register(ComponentSerializer)
                    it.register(StyleBuilderApplicableSerializer)
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

//<editor-fold desc="Basic Serializers">
internal object KeySerializer : TypeSerializer<Key> {
    override fun deserialize(type: Type, node: ConfigurationNode): Key = Key.key(node.requireKt<String>())
    override fun serialize(type: Type?, obj: Key?, node: ConfigurationNode?): Nothing = throw UnsupportedOperationException()
}

internal object IntRangeParser : TypeSerializer<Range<Int>> {
    override fun deserialize(type: Type, node: ConfigurationNode): Range<Int> = RangeParser.parseIntRange(node.requireKt<String>())
    override fun serialize(type: Type, obj: Range<Int>?, node: ConfigurationNode): Nothing = throw UnsupportedOperationException()
}

internal object ComponentSerializer : ScalarSerializer<Component>(Component::class.java) {
    override fun deserialize(type: Type, obj: Any): Component {
        val message = obj.toString().replace("§", "")
        return GlobalContext.get().get<MiniMessage>().deserialize(message)
    }

    override fun serialize(item: Component, typeSupported: Predicate<Class<*>>?): Any {
        return GlobalContext.get().get<MiniMessage>().serialize(item)
    }
}

internal object StyleBuilderApplicableSerializer :
    ScalarSerializer<Array<StyleBuilderApplicable>>(typeTokenOf<Array<StyleBuilderApplicable>>()) {
    override fun deserialize(type: Type, obj: Any): Array<StyleBuilderApplicable> {
        val component = ComponentSerializer.deserialize(type, obj)
        val styleList = ArrayList<StyleBuilderApplicable>()

        with(component) {
            // font()?.let { font -> styleList += StyleBuilderApplicable { it.font(font) } }

            color()?.let { styleList += it }

            TextDecoration.entries
                .filter { decoration(it) == TextDecoration.State.TRUE }
                .map { it.withState(decoration(it)) }
                .forEach { styleList += it }

            // clickEvent()?.let { styleList += it }

            // hoverEvent()?.let { styleList += it }
        }

        return styleList.toTypedArray()
    }

    override fun serialize(item: Array<StyleBuilderApplicable>, typeSupported: Predicate<Class<*>>?): Any {
        val component = Component.text().style { builder -> item.forEach(builder::apply) }.build()
        return GlobalContext.get().get<MiniMessage>().serialize(component)
    }
}
//</editor-fold>